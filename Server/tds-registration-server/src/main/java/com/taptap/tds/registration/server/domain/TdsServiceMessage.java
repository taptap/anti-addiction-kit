package com.taptap.tds.registration.server.domain;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * @Author guyu
 * @create 2021/1/20 8:49 下午
 */
public class TdsServiceMessage extends TdsMessage<String> {

    @JsonRawValue
    @Override
    public String getBody() {
        return super.getBody();
    }
}
