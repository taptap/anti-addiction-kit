package com.taptap.tds.registration.server.util;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

@Log4j2
public class SignUtil {

    public static final String HMAC_SHA256 = "HmacSHA256";
    public static String generateSignature(Map<String, String> parameters, String body, String secretKey) {
        String signData = buildData(parameters, body, secretKey);
        log.info("The data to sign in header is [{}]", signData);
        String result = sign(signData);
        log.info("sign in [{}]", result);
        return result;
    }

    public static String buildData(Map<String, String> parameters, String body, String secretKey) {
        String[] sortedKeys = (String[])parameters.keySet().toArray(new String[0]);
        Arrays.sort(sortedKeys);
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(secretKey);
        String[] var5 = sortedKeys;
        int var6 = sortedKeys.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String key = var5[var7];
            stringToSign.append(key).append((String)parameters.get(key));
        }

        return stringToSign.append(body).toString();
    }

    public static String getSignature(String msg, String secretKey) {
        return hamcsha256(msg.getBytes(), secretKey.getBytes());
    }

    private static String hamcsha256(byte[] data, byte[] key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(signingKey);
            return Hex.encodeHexString(mac.doFinal(data)).toUpperCase();
        } catch (NoSuchAlgorithmException var4) {
            var4.printStackTrace();
        } catch (InvalidKeyException var5) {
            var5.printStackTrace();
        }

        return null;
    }

    public static String sign(String toBeSignStr) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(toBeSignStr.getBytes(StandardCharsets.UTF_8));
            return byteToHexString(messageDigest.digest());
        } catch (Exception var2) {
            log.error(var2.getMessage(), var2);
            return null;
        }
    }

    private static String byteToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        byte[] var2 = bytes;
        int var3 = bytes.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte aByte = var2[var4];
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

}
