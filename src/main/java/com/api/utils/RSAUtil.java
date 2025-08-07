package com.api.utils;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public final class RSAUtil {
    public static final String RSA_PRIVATEKEY = "MIIC...";
        // Key is truncated for brevity

    public static String decrypt(String str) {
        try {
            // base64 String
            byte[] inputByte = Base64Util.convertStringToByteArray(str);
            // base64 privateKey
            byte[] decoded = Base64Util.convertStringToByteArray(RSA_PRIVATEKEY);
            RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(decoded));
            // RSA decrypt
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            return new String(cipher.doFinal(inputByte));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}