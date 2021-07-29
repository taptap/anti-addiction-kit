package com.taptap.tds.registration.server.service;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.taptap.tds.registration.server.AuthorizationException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author guyu
 * @create 2020/12/30 2:44 下午
 */
@Log4j2
@Component
public class TokenVerifier {

    @Autowired
    private JWTVerifier jwtVerifier;

    public String verifyTokenAndGetUserId(String token){

        if (StringUtils.isBlank(token)) {
            throw new AuthorizationException("token is blank");
        }

        DecodedJWT jwt = null;
        try {
            jwt = jwtVerifier.verify(token);
        }catch (JWTVerificationException jwtExcetion){
            throw new AuthorizationException("signature is invalid");
        }
        String userId = jwt.getClaim("user_id").asString();
        if(StringUtils.isBlank(userId)){
            throw new AuthorizationException("user_id is null");
        }
        return userId;
    }

}
