package com.taptap.tds.registration.server.util;

/**
 * @Author: SunYi
 * @Date: 2020/8/11 2:51 下午
 * @Description: 预设的返回码
 */
public enum ResultCode {
    //
    ILLEGAL_PARAMETERS(400, "illegal_parameters", "Illegal Parameters"),
    ILLEGAL_CLIENT(400, "illegal_client", "Illegal client"),
    ILLEGAL_GAME_ID(400, "illegal_gameId", "Illegal GameId"),
    INVALID_GRANT(400, "invalid_grant", "Invalid Grant"),
    UNAUTHORIZED(401, "unauthorized", "Unauthorized"),
    NOT_FOUND(404, "not_found", "Not Found"),
    INTERNAL_SERVER_ERROR(500, "internal_server_error", "Internal Server Error"),
    REQUEST_METHOD_NOT_SUPPORTED(405, "request_method_not_supported", "Request Method Not Supported"),
    BUSINESS_ERROR(480, "business_error", "Business Error"),
    CONFLICT(409, "conflict", "Conflict"),
    FORBIDDEN(403, "forbidden","Forbidden"),
    TOO_MANY_REQUESTS(429, "too many requests", "Too Many Requests");

    //错误代码
    private final Integer code;
    //错误码
    private final String error;
    //错误描述
    private final String errorDescription;

    ResultCode(Integer code, String error, String errorDescription) {
        this.code = code;
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public Integer getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
