package com.taptap.tds.registration.server.endpoints;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taptap.tds.registration.server.ApiResponseDto;
import com.taptap.tds.registration.server.configuration.BeanMapper;
import com.taptap.tds.registration.server.configuration.PublicityProperties;
import com.taptap.tds.registration.server.configuration.TdsPushProperties;
import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.domain.IdentificationDetails;
import com.taptap.tds.registration.server.dto.AddictionRequest;
import com.taptap.tds.registration.server.dto.PublicityResult;
import com.taptap.tds.registration.server.dto.rpc.IdentificationInfo;
import com.taptap.tds.registration.server.dto.rpc.TdsIdentificationRequest;
import com.taptap.tds.registration.server.enums.IdentificationStatus;
import com.taptap.tds.registration.server.manager.ReactiveIdentificationDetailsManager;
import com.taptap.tds.registration.server.service.PublicityClient;
import com.taptap.tds.registration.server.util.SignUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

@Log4j2
@RestController
@Validated
public class IdentificationEndpoint {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private TdsPushProperties tdsPushProperties;

    @Autowired
    private BeanMapper beanMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReactiveIdentificationDetailsManager reactiveIdentificationDetailsManager;

    @Autowired
    private PublicityClient client;

    @Autowired
    private PublicityProperties properties;

    //身份证前1位每位加权因子
    private static int[] power = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    //身份证第18位校检码
    private static String[] refNumber ={"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
    //省(直辖市)代码表
    private static HashSet provinceCodes = new HashSet();

    static{
        provinceCodes.addAll(Arrays.asList("11", "12", "13", "14", "15", "21", "22",
                "23", "31", "32", "33", "34", "35", "36", "37", "41", "42", "43",
                "44", "45", "46", "50", "51", "52", "53", "54", "61", "62", "63",
                "64", "65", "71", "81", "82", "91"));
    }

    @PostMapping(value = "/api/v1/identification", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public Mono<?> identifyUser(@RequestBody @Validated TdsIdentificationRequest request) {

        try {
            if(!isValid(request.getIdCard())){
                return Mono.just(new ApiResponseDto(400,"身份证格式错误"));
            }} catch(Exception e){
            log.debug("exception happens : {}" ,e);
            return Mono.just(new ApiResponseDto(400,"身份证格式错误"));
        }

        return reactiveIdentificationDetailsManager.findByUserId(request.getUserId(),
                FieldsExpand.createWithFields("id", "status"))
                .flatMap(existIdentificationDetails->{
                    if(existIdentificationDetails.getStatus()!=IdentificationStatus.FAILED){
                        IdentificationInfo identificationInfo = new IdentificationInfo();
                        identificationInfo.setIdentifyState(existIdentificationDetails.getStatus());
                        return Mono.just(new ApiResponseDto(400,"已有实名信息", identificationInfo));
                    }
                    // 如果原来的失败了，不校验参数是否有变化直接删除后重试？
                    return reactiveIdentificationDetailsManager.delete(existIdentificationDetails).then(tryIdentify(request));
                }).switchIfEmpty(tryIdentify(request));

    }

    private boolean isValid(String idNum){

       return checkProvinceId(idNum.substring(0,2)) &&
              isValidDate(idNum.substring(6,14)) &&
              checkCardIdLastNum(idNum);
    }

    private boolean checkProvinceId(String provinceid){
        return provinceCodes.contains(provinceid);
    }

    public static boolean checkCardIdLastNum(String cardId){

        if(!StringUtils.isNumeric(cardId.substring(0,17))){
            return false;
        }

        char[] tmp = cardId.toCharArray();
        int[] array = new int[tmp.length-1];
        for(int i=0;i<tmp.length-1;i++){
            array[i] = tmp[i] - '0';
        }
        String checkCode = sumPower(array);
        String lastChar = cardId.charAt(17)+"";
        if(lastChar.equals("x")){
            lastChar = lastChar.toUpperCase();
        }
        return checkCode.equals(lastChar);
    }

    private static String sumPower(int[] array){
        int result = 0;
        for(int i=0;i<power.length;i++){
            result += power[i] * array[i];
        }
        return refNumber[(result%11)];
    }

    // 调用前保证为8为数字
    private boolean isValidDate(String date) {
        try {
            formatter.parse(date.trim());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Mono<ApiResponseDto> tryIdentify(TdsIdentificationRequest request){
        IdentificationDetails identificationDetails = beanMapper.map(request, IdentificationDetails.class);
        String ai = UUID.randomUUID().toString().replace("-", "");
        identificationDetails.setAi(ai);
        AddictionRequest addictionRequest = client.toRequest(request, ai);

        IdentificationInfo identificationInfo = new IdentificationInfo();
        identificationInfo.setIdentifyState(IdentificationStatus.FAILED);

        return uploadToPublicity(addictionRequest)
                .flatMap(result -> {
                    if (result.getErrcode() == 0 && StringUtils.equalsIgnoreCase(result.getErrmsg(), "OK")) {

                        String pi = result.getData().getResult().getPi();
                        IdentificationStatus status = result.getData().getResult().getStatus();
                        identificationDetails.setPi(pi);
                        identificationDetails.setStatus(status);

                        identificationInfo.setIdentifyState(identificationDetails.getStatus());
                        identificationInfo.setAntiAddictionToken(getAntiAddictionToken(identificationDetails));

                    } else {
                        log.info("【push failed】push id to publicity failed! ErrorCode is {} , msg is {}", result.getErrcode(), result.getErrmsg());
                        identificationDetails.setStatus(IdentificationStatus.FAILED);
                        identificationInfo.setAntiAddictionToken(getAntiAddictionToken(identificationDetails));

                    }
                    return reactiveIdentificationDetailsManager.save(identificationDetails)
                            .then(Mono.just(new ApiResponseDto(identificationInfo)));
                }).onErrorResume(e->{
                    log.error("error when identifying e is {}", e);
                    return Mono.just(new ApiResponseDto(500,"系统异常,请稍后再试"));
                });
    }

    private Mono<PublicityResult> uploadToPublicity(AddictionRequest addictionRequest) {

        String s;
        try {
            s = objectMapper.writeValueAsString(addictionRequest);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap();
        map.add("appId", this.properties.getAppId());
        map.add("bizId", properties.getBizId());
        long timestamp = System.currentTimeMillis();
        map.add("timestamps", Long.toString(timestamp));
        String sign = SignUtil.generateSignature(map.toSingleValueMap(), s, this.properties.getSignKey());
        map.add("sign", sign);
        return WebClient.create(properties.getIdentificationRootUri())
                .post()
                .uri("/authentication/check")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.addAll(map))
                .bodyValue(s)
                .retrieve()
                .bodyToMono(PublicityResult.class);
    }

    @GetMapping("/api/v1/identification/info")
    public Mono<?> get(@NotBlank String user_id){

        return reactiveIdentificationDetailsManager.findByUserId(user_id, null).map(identificationDetails -> {
            IdentificationInfo identificationInfo = new IdentificationInfo();
            identificationInfo.setIdentifyState(identificationDetails.getStatus());
            identificationInfo.setIdCard(identificationDetails.getIdCard().replaceAll("(\\w{6})\\w*(\\w{3})", "$1*********$2"));
            identificationInfo.setName(identificationDetails.getName());
            identificationInfo.setUserId(identificationDetails.getUserId());
            identificationInfo.setAntiAddictionToken(getAntiAddictionToken(identificationDetails));
            return new ApiResponseDto(identificationInfo);
        }).switchIfEmpty(Mono.just(new ApiResponseDto(getEmptyIdentificationDetails(user_id))));
    }

    private String getAntiAddictionToken(IdentificationDetails identificationDetails){

        if(identificationDetails.getStatus() != IdentificationStatus.FAILED) {

            String idCard = identificationDetails.getIdCard();
            String birthday = idCard.substring(6, 10) + "-"
                    + idCard.substring(10, 12) + "-"
                    + idCard.substring(12, 14);

            return JWT.create().withClaim("unique_id", DigestUtils.md5DigestAsHex(idCard.getBytes(StandardCharsets.UTF_8)))
                    .withClaim("user_id", identificationDetails.getUserId())
                    .withClaim("birthday", birthday).sign(Algorithm.HMAC256(tdsPushProperties.getJws()));
        } else{
            return JWT.create().withClaim("unique_id", "")
                    .withClaim("user_id", identificationDetails.getUserId())
                    .withClaim("birthday", "").sign(Algorithm.HMAC256(tdsPushProperties.getJws()));
        }
    }

    private IdentificationInfo getEmptyIdentificationDetails(String userId){
        IdentificationInfo identificationInfo = new IdentificationInfo();
        // 前端要默认值。。 ：
        identificationInfo.setName("");
        identificationInfo.setIdCard("");
        identificationInfo.setUserId(userId);
        identificationInfo.setIdentifyState(IdentificationStatus.SUCCESS);
        String token = JWT.create().withClaim("unique_id", "")
                .withClaim("user_id", userId)
                .withClaim("birthday", "").sign(Algorithm.HMAC256(tdsPushProperties.getJws()));
        identificationInfo.setAntiAddictionToken(token);
        return identificationInfo;
    }

}