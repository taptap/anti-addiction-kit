package com.taptap.tds.registration.server.domain;


import java.util.HashMap;
import java.util.Map;

/**
 * @Author guyu
 * @create 2021/1/18 2:01 下午
 */

public class TdsMessage<T> {

    private Map<String, String> header = new HashMap();

    private T body;

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
