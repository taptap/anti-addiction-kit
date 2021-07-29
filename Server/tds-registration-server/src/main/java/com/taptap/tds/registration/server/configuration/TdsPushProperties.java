package com.taptap.tds.registration.server.configuration;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author guyu
 * @create 2021/1/8 5:08 下午
 */
@ConfigurationProperties("tds.push")
@Log4j2
public class TdsPushProperties {

    private String jws = "123456";

    private static final Long DEFAULT_TTL = TimeUnit.DAYS.toSeconds(1);

    private Long ttl = DEFAULT_TTL;

    private Long healthCheckIdle = 10000l;

    private int pushTimeOut = 60;

    private Map<String, String> pathPrefixTopicsMapping = new HashMap<>();

    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public Map<String, String> getPathPrefixTopicsMapping() {
        return pathPrefixTopicsMapping;
    }

    public void setPathPrefixTopicsMapping(Map<String, String> pathPrefixTopicsMapping) {
        this.pathPrefixTopicsMapping = pathPrefixTopicsMapping;
    }

    public Long getHealthCheckIdle() {
        return healthCheckIdle;
    }

    public void setHealthCheckIdle(Long healthCheckIdle) {
        this.healthCheckIdle = healthCheckIdle;
    }

    public int getPushTimeOut() {
        return pushTimeOut;
    }

    public void setPushTimeOut(int pushTimeOut) {
        this.pushTimeOut = pushTimeOut;
    }
}
