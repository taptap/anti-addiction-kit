package com.taptap.tds.registration.server.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.taptap.tds.registration.server.configuration.BeanMapper;
import com.taptap.tds.registration.server.domain.UserAction;
import com.taptap.tds.registration.server.dto.*;
import com.taptap.tds.registration.server.dto.rpc.TdsIdentificationRequest;
import com.taptap.tds.registration.server.util.EncryptSignUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;

@Log4j2
@Service
public class PublicityClient {

    private ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
                .featuresToDisable(
            JsonGenerator.Feature.IGNORE_UNKNOWN,
            MapperFeature.DEFAULT_VIEW_INCLUSION,
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
            )
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();

    @Resource
    private RestTemplate userActionRestTemplate;

    @Resource
    private RestTemplate checkRestTemplate;

    @Resource
    private RestTemplate identificationRestTemplate;

    @Autowired
    private BeanMapper beanMapper;

    public PublicityResult uploadUserAction(List<UserAction> userActions){
        ResponseEntity<PublicityResult> responseEntity = this.userActionRestTemplate.postForEntity("/collection/loginout", toRequest(userActions), PublicityResult.class);
        return responseEntity.getBody();
    }

    public PublicityResult uploadIdentification(TdsIdentificationRequest tdsIdentificationRequest , String ai){
        return identificationRestTemplate.postForEntity("/authentication/check", toRequest(tdsIdentificationRequest, ai), PublicityResult.class ).getBody();
    }

    public PublicityResult checkIdentification(String ai){
        return checkRestTemplate.getForEntity("/authentication/query?ai={1}", PublicityResult.class,  ai).getBody();
    }

    private AddictionRequest toRequest(List<UserAction> userActions) {
        List<InnerData> list = new LinkedList();
        int i = 1;

        for(UserAction userAction : userActions) {
            InnerData innerData = new InnerData();
            innerData.setNo(i);
            innerData.setSi(userAction.getSessionId());
            innerData.setBt(userAction.getActionType().getValue());
            innerData.setOt(userAction.getActionTime().toEpochMilli() / 1000L);
            innerData.setCt(userAction.getUserType().getValue());
            innerData.setDi(userAction.getDeviceId());
            innerData.setPi(userAction.getPi());
            list.add(innerData);
            i++;
        }
        AddictionData addictionData = new AddictionData();
        addictionData.setCollections(list);
        return encryptToRequest(addictionData);
    }

    public AddictionRequest toRequest(TdsIdentificationRequest tdsIdentificationRequest, String ai) {
        IdentificationRequest request = beanMapper.map(tdsIdentificationRequest, IdentificationRequest.class);
        request.setIdNum(tdsIdentificationRequest.getIdCard());
        request.setAi(ai);
        return encryptToRequest(request);
    }

//    private String generateAi(String userId){
//        return userId;
//    }

    private AddictionRequest encryptToRequest(Object object){

        AddictionRequest request = new AddictionRequest();
        String data;
        try {
            data = this.objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            log.info(e.getMessage(), e);
            throw new UncheckedIOException(e);
        }
        request.setData(EncryptSignUtil.aesGcmEncrypt(data));
        return request;
    }

}
