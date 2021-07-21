package com.tapsdk.antiaddiction.utils;

import android.content.Context;
import android.content.res.AssetManager;


import com.tapsdk.antiaddiction.skynet.okio.BufferedSource;
import com.tapsdk.antiaddiction.skynet.okio.Okio;

import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    public static String getJsonFromAssetsFile(Context context, String fileName){
        if (Util.checkIsNull(context)) return "";

        String result = "";
        AssetManager assetManager = context.getAssets();

        try {
            InputStream is = assetManager.open(fileName);
            BufferedSource source = Okio.buffer(Okio.source(is));
            result = source.readUtf8();
            source.close();
        } catch (IOException e) {
            AntiAddictionLogger.e("getJsonFromAssetsFile:" + e.getCause() + " | " + e.getMessage());
        }
        return result;
    }
}
