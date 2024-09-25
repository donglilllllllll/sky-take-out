
package com.sky.config;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue =
            new byte[]{'T', 'h', 'i', 's', 'I', 's', 'A', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'}; // 这是一个示例密钥，实际使用时请更换为更安全的密钥


    /**
     * AES加密
     *
     * @param data 待加密的数据
     * @return 加密后的Base64编码字符串
     */

    public static String encrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = c.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }


    /**
     * AES解密
     *
     * @param encryptedData 已加密的Base64编码字符串
     * @return 解密后的原始数据
     */

    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = c.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }


    /**
     * 生成AES密钥
     *
     * @return 密钥对象
     */

    private static Key generateKey() throws Exception {
        return new SecretKeySpec(keyValue, ALGORITHM);
    }

    public static void main(String[] args) throws Exception {

        String passwd123 = encrypt("123456");
        System.out.println(passwd123);
        System.out.println(decrypt(passwd123));

    }
}
