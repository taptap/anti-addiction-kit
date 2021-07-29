package com.taptap.tds.registration.server;

import com.taptap.tds.registration.server.configuration.PublicityProperties;
import com.taptap.tds.registration.server.util.SignUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Component
public class SignInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private PublicityProperties properties;

    private static final Pattern QUERY_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {


        String bodyStr = new String(body, "UTF-8");
        MultiValueMap<String, String> map = new LinkedMultiValueMap();
        map.add("appId", properties.getAppId());
        map.add("bizId", properties.getBizId());
        long timestamp = System.currentTimeMillis();
        map.add("timestamps", Long.toString(timestamp));
        map.addAll(queryParams(request.getURI().getQuery()));
        String sign = SignUtil.generateSignature(map.toSingleValueMap(), bodyStr, this.properties.getSignKey());
        map.add("sign", sign);
        request.getHeaders().addAll(map);
        return execution.execute(request, body);
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