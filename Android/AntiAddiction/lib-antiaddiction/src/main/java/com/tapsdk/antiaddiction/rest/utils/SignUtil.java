package com.tapsdk.antiaddiction.rest.utils;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUtil {

    private static String byteToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            String strHex = Integer.toHexString(aByte);
            if (strHex.length() > 3) {
                sb.append(strHex.substring(6));
            } else if (strHex.length() < 2) {
                sb.append("0").append(strHex);
            } else {
                sb.append(strHex);
            }
        }
        return sb.toString();
    }

    public static String getSignCode(String codeVerifier) {
        String result = "";
        byte[] signCode = null;
        if (TextUtils.isEmpty(codeVerifier)) {
            return null;
        }
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(codeVerifier.getBytes());
            signCode = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (signCode != null) {
            result = byteToHexString(signCode);
        }

        return result;
    }
}
