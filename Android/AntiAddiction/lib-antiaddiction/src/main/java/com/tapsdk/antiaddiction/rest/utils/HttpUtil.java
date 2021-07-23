package com.tapsdk.antiaddiction.rest.utils;

import com.tapsdk.antiaddiction.skynet.okhttp3.RequestBody;
import com.tapsdk.antiaddiction.skynet.okio.Buffer;

import java.io.IOException;

public class HttpUtil {

    public static String bodyToString(RequestBody requestBody) throws IOException {
        final Buffer buffer = new Buffer();
        if(requestBody != null)
            requestBody.writeTo(buffer);
        else
            return "";
        return buffer.readUtf8();
    }
}
