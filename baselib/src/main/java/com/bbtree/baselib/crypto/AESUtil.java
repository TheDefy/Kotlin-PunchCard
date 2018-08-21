package com.bbtree.baselib.crypto;
/**
 * Java Cryptography Extension (JCE)中使用AES算法加密时，当密钥大于128时，代码会抛出java.security.In
 * validKeyException: Illegal key size or default parameters，去掉这种限制需要下
 * 载Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files.
 * <p/>
 * JCE中AES支持五中模式：CBC，CFB，ECB，OFB，PCBC；
 * 支持三种填充：NoPadding，PKCS5Padding，ISO10126Padding。
 * 不支持SSL3Padding。不支持“NONE”模式。
 * <p/>
 * 其中AES/ECB/NoPadding和我现在使用的AESUtil得出的结果相同(在16的整数倍情况下)。
 * 不带模式和填充来获取AES算法的时候，其默认使用ECB/PKCS5Padding。
 * <p/>
 * 算法/模式/填充              16字节加密后数据长度       不满16字节加密后长度
 * AES/CBC/NoPadding              16                  不支持
 * AES/CBC/PKCS5Padding           32                  16
 * AES/CBC/ISO10126Padding        32                  16
 * AES/CFB/NoPadding              16                  原始数据长度
 * AES/CFB/PKCS5Padding           32                  16
 * AES/CFB/ISO10126Padding        32                  16
 * AES/ECB/NoPadding              16                  不支持
 * AES/ECB/PKCS5Padding           32                  16
 * AES/ECB/ISO10126Padding        32                  16
 * AES/OFB/NoPadding              16                  原始数据长度
 * AES/OFB/PKCS5Padding           32                  16
 * AES/OFB/ISO10126Padding        32                  16
 * AES/PCBC/NoPadding             16                  不支持
 * AES/PCBC/PKCS5Padding          32                  16
 * AES/PCBC/ISO10126Padding       32                  16
 * <p/>
 * 可以看到，在原始数据长度为16的整数倍时，假如原始数据长度等于16*n，则使用NoPadding时加密后数据长度等于16*n，其它情
 * 况下加密数据长度等于16*(n+1)。在不足16的整数倍的情况下，假如原始数据长度等于16*n+m[其中m小于16]，除了NoPadding填充
 * 之外的任何方式，加密数据长度都等于16*(n+1)；NoPadding填充情况下，CBC、ECB和PCBC三种模式是不支持的，CFB、OFB两种
 * 模式下则加密数据长度等于原始数据长度。
 */

import com.bbtree.baselib.utils.FileUtils;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AESUtil {
    // 共通鍵
//    private static final String ENCRYPTION_KEY = "m00HKb78";
//    private static final String ENCRYPTION_IV = "V0m87mQJnh8s19Ph";
    private static final String TRANSFORMATION = "AES/CFB/NoPadding";
    private static final String TAG = AESUtil.class.getSimpleName();

    public static String encrypt(String src, String key, String ivSpec) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, makeKey(key), makeIv(ivSpec));
            return Base64.encodeBytes(cipher.doFinal(src.getBytes("utf-8")));
        } catch (Exception e) {
            Logger.d(e);
        }
        return "";
    }

    public static String decrypt(String src, String key, String ivSpec) {
        String decrypted = "";
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, makeKey(key), makeIv(ivSpec));
            decrypted = new String(cipher.doFinal(Base64.decode(src)), "utf-8");
        } catch (Exception e) {
            Logger.d(e);
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


    public static void main(String[] args) {
        String secret = "0c6313ab4d5d8f4d9aae848d046fff4d";
        String s = FileUtils.readFileSdcardFile("/Users/zzz/Downloads/" + "20170104.log");
        System.out.print(s);
        String decrypt = decrypt(s, secret.substring(0, secret.length() - 16), secret.substring(secret.length() - 16, secret.length()));
        FileUtils.writeFileSdcardFile("/Users/zzz/Downloads/"+"20170104.txt",decrypt);
    }
}
