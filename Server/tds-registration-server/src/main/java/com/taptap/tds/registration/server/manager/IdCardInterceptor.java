package com.taptap.tds.registration.server.manager;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.manager.interceptor.AbstractManagerInterceptor;
import com.taptap.tds.registration.server.domain.IdentificationDetails;
import com.taptap.tds.registration.server.util.Cryptor;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class IdCardInterceptor extends AbstractManagerInterceptor<IdentificationDetails> {

    private final Cryptor crypt;

    public IdCardInterceptor(Cryptor crypt) {
        super();
        this.crypt = crypt;
    }

    @Override
    protected void postQueryInternal(IdentificationDetails entity, FieldsExpand fieldsExpand) {
        String idCard = entity.getIdCard();
        if (StringUtils.isNotEmpty(idCard)) {
            entity.setIdCard(new String(crypt.decryptHexString(idCard), StandardCharsets.UTF_8));
        }
    }

    @Override
    protected void preHandle(IdentificationDetails entity) {
        String idCard = entity.getIdCard();
        if (StringUtils.isNotEmpty(idCard)) {
            entity.setIdCard(crypt.encryptToHexString(idCard));
        }
    }
}