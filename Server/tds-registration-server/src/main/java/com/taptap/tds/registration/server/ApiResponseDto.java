package com.taptap.tds.registration.server;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ApiResponseDto<T> implements Serializable {

    private Integer code = 200;

    private String msg;

    private T data;

    public ApiResponseDto(T data) {
        this.data = data;
    }

    public ApiResponseDto(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
