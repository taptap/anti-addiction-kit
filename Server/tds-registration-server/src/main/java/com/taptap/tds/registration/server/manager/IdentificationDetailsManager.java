package com.taptap.tds.registration.server.manager;

import com.taptap.tds.registration.server.configuration.PublicityProperties;
import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.persistence.mybatis.manager.BaseManager;
import com.taptap.tds.registration.server.domain.IdentificationDetails;
import com.taptap.tds.registration.server.enums.IdentificationStatus;
import com.taptap.tds.registration.server.mapper.IdentificationDetailsMapper;
import com.taptap.tds.registration.server.util.Cryptor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IdentificationDetailsManager extends BaseManager<IdentificationDetails, Long, IdentificationDetailsMapper> {

    public IdentificationDetailsManager(PublicityProperties publicityProperties) {
        addManagerInterceptor(new IdCardInterceptor(new Cryptor(publicityProperties.getSecretKey())));
    }

    public IdentificationDetails findByUserId(String userId, FieldsExpand fieldsExpand){
        IdentificationDetails result = mapper.findByUserId(userId, fieldsExpand);
        postQuery(result,fieldsExpand);
        return result;
    }

    public List<IdentificationDetails> findByUserIdIn(List<String> userIds, FieldsExpand fieldsExpand){
        List<IdentificationDetails> results = mapper.findByUserIdIn(userIds, fieldsExpand);
        postQuery(results,fieldsExpand);
        return results;
    }

    public List<IdentificationDetails> findByStatus(IdentificationStatus status, FieldsExpand fieldsExpand){
        List<IdentificationDetails> results = mapper.findByStatus(status, fieldsExpand);
        postQuery(results,fieldsExpand);
        return results;
    }
}
