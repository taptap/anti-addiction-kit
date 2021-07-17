package com.tapsdk.antiaddiction.skynet.retrofit2.adapter.converter;

import com.tapsdk.antiaddiction.skynet.okhttp3.ResponseBody;
import com.tapsdk.antiaddiction.skynet.retrofit2.Converter;

import java.io.IOException;

public class StringResponseBodyConverter implements Converter<ResponseBody, String> {

    @Override
    public String convert(ResponseBody value) throws IOException {
        return value.string();
    }
}
