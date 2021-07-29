package com.taptap.tds.registration.server.service;

import com.taptap.tds.registration.server.AuthorizationException;
import com.taptap.tds.registration.server.session.GameUserPrinciple;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.socket.HandshakeInfo;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author guyu
 * @create 2021/3/4 11:53 上午
 */
@Log4j2
@Component
public class XDAuthorizationService implements AuthorizationService{

    private static String TOKEN_KEY = "Authorization";

    private static final Pattern QUERY_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");

    @Autowired
    private TokenVerifier tokenVerifier;

    @Override
    public Mono<GameUserPrinciple> getGameUserPrinciple(HandshakeInfo handshakeInfo) {

        String token = handshakeInfo.getHeaders().getFirst(TOKEN_KEY);

        String rawQuery = handshakeInfo.getUri().getRawQuery();
        MultiValueMap<String, String> stringStringMultiValueMap = queryParams(rawQuery);
        if(StringUtils.isBlank(token)){
            token = stringStringMultiValueMap.getFirst(TOKEN_KEY);
        }
//        log.debug("[Login] token is {}", token);
        String userId;
        try {
            userId = tokenVerifier.verifyTokenAndGetUserId(token);
        }catch (AuthorizationException e){
            return Mono.error(e);
        }
//        final String gameId = stringStringMultiValueMap.getFirst("client_id");

//        if(StringUtils.isBlank(gameId)){
//            return Mono.error(new AuthorizationException("Illegal Parameters"));
//        }
//        log.debug("[Login] gameId is {}, userId is {}", gameId, userId);
        return Mono.just(new GameUserPrinciple("gameId", userId));
    }

    protected MultiValueMap<String, String> queryParams(String query) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        if (query != null) {
            Matcher matcher = QUERY_PATTERN.matcher(query);
            while (matcher.find()) {
                String name = decodeQueryParam(matcher.group(1));
                String eq = matcher.group(2);
                String value = matcher.group(3);
                value = (value != null ? decodeQueryParam(value) : (org.springframework.util.StringUtils.hasLength(eq) ? "" : null));
                queryParams.add(name, value);
            }
        }
        return queryParams;
    }

    private String decodeQueryParam(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Could not decode query value [" + value + "] as 'UTF-8'. " +
                        "Falling back on default encoding: " + ex.getMessage());
            }
            return URLDecoder.decode(value);
        }
    }
}
