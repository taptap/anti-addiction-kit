package com.taptap.tds.registration.server;

/**
 * @Author guyu
 * @create 2021/2/18 4:25 下午
 */
public class AuthorizationException extends RuntimeException{

    public AuthorizationException(String message) {
        super(message);
    }
}
