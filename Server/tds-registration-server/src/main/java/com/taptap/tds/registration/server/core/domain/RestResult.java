package com.taptap.tds.registration.server.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RestResult<T> {

    private final List<T> result;

    @JsonCreator
    public RestResult(@JsonProperty("result") List<T> result) {
        this.result = result;
    }

    public List<T> getResult() {
        return result;
    }
}
