package com.taptap.tds.registration.server;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: SunYi
 * @Date: 2020/8/11 2:42 下午
 * @Description: 标准错误内容
 */
@Data
@AllArgsConstructor
public class ErrorDataDto implements Serializable {

    /**
     * 错误数字代码
     */
    private Integer code;
    /**
     * 错误码
     */
    private String error;
    /**
     * 错误码
     */
    private String errorDescription;
    /**
     * 错误码
     */
    private String msg;

}

