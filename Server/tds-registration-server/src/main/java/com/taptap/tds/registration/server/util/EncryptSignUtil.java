package com.taptap.tds.registration.server.util;

import com.taptap.tds.registration.server.configuration.PublicityProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class EncryptSignUtil implements InitializingBean {

    private static PublicityProperties publicityProperties;

    private static byte[] keyBytes;

    public void afterPropertiesSet() throws Exception {
        keyBytes = hexStringToByte(publicityProperties.getSignKey());
    }

    @Autowired
    public void setPublicityProperties(PublicityProperties publicityProperties) {
        EncryptSignUtil.publicityProperties = publicityProperties;
    }

    public static String aesGcmEncrypt(String content) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
            SecretKeySpec skey = new SecretKeySpec(keyBytes, "AES");
            cipher.init(1, skey);
            byte[] ivb = cipher.getIV();
            byte[] encodedByteArray = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            byte[] message = new byte[ivb.length + encodedByteArray.length];
            System.arraycopy(ivb, 0, message, 0, ivb.length);
            System.arraycopy(encodedByteArray, 0, message, ivb.length, encodedByteArray.length);
            return Base64.getEncoder().encodeToString(message);
        } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException var6) {
            return null;
        }
    }

    public static String aesGcmDecrypt(String content) {
        try {
            Cipher decryptCipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
            SecretKeySpec skey = new SecretKeySpec(keyBytes, "AES");
            byte[] encodedArrayWithIv = Base64.getDecoder().decode(content);
            GCMParameterSpec decryptSpec = new GCMParameterSpec(128, encodedArrayWithIv, 0, 12);
            decryptCipher.init(2, skey, decryptSpec);
            byte[] b = decryptCipher.doFinal(encodedArrayWithIv, 12, encodedArrayWithIv.length - 12);
            return new String(b, StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException var6) {
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

    private static byte[] hexStringToByte(String str) {
        byte[] baKeyword = new byte[str.length() / 2];

        for(int i = 0; i < baKeyword.length; ++i) {
            try {
                baKeyword[i] = (byte)(255 & Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

        return baKeyword;
    }
}
