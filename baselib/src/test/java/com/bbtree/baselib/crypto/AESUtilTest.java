package com.bbtree.baselib.crypto;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by qiujj on 2017/6/9.
 */
public class AESUtilTest {
    @Test
    public void encrypt() throws Exception {

    }

    @Test
    public void testDecrypt() throws Exception {
        String secret = "0c631d046fff4d3ab4d5d8f4d9aae848";
        String s = "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";
        String decrypt = decrypt(s, secret.substring(0, secret.length() - 16), secret.substring(secret.length() - 16, secret.length()));
        System.out.print(decrypt);
    }

    public static String decrypt(String src, String key, String ivSpec) {
        String decrypted = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, makeKey(key), makeIv(ivSpec));
            decrypted = new String(cipher.doFinal(Base64.decode(src)), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }


    private static AlgorithmParameterSpec makeIv(String ENCRYPTION_IV) {
        try {
            return new IvParameterSpec(ENCRYPTION_IV.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Key makeKey(String ENCRYPTION_KEY) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] key = md.digest(ENCRYPTION_KEY.getBytes("UTF-8"));
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

}