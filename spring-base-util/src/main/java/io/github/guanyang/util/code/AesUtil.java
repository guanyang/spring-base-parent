package io.github.guanyang.util.code;

import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;

import com.google.common.collect.Maps;
import java.security.GeneralSecurityException;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

/**
 * 功能描述：AES加密工具类
 * <li>参考文章：https://www.cnblogs.com/caizhaokai/p/10944667.html</li>
 *
 * @author gy
 * @version 1.0.0
 */
@Slf4j
public class AesUtil {

    private static final String AES = "AES";
    private static final String SPLIT = "-";

    private static final Map<String, Cipher> encryptCipherCache = Maps.newConcurrentMap();

    private static final Map<String, Cipher> decryptCipherCache = Maps.newConcurrentMap();


    /**
     * 功能描述：加密
     *
     * @param key 加密key
     * @param salt 加密盐值
     * @param originalText 明文
     * @param aesEnum 加解密类型
     * @return 密文
     * @author gy
     * @version 1.0.0
     */
    public static String encrypt(String key, String salt, String originalText, AesEnum aesEnum) {
        if (StringUtils.isBlank(originalText)) {
            return null;
        }
        Cipher cipher = getEncryptCipher(key, salt, aesEnum);
        if (cipher == null) {
            return null;
        }
        return encrypt(cipher, originalText);
    }


    /**
     * 功能描述：解密
     *
     * @param key 加密key
     * @param salt 加密盐值
     * @param cipherText 密文
     * @param aesEnum 加解密类型
     * @return 明文
     * @author gy
     * @version 1.0.0
     */
    public static String decrypt(String key, String salt, String cipherText, AesEnum aesEnum) {
        if (StringUtils.isBlank(cipherText)) {
            return null;
        }
        Cipher cipher = getDecryptCipher(key, salt, aesEnum);
        if (cipher == null) {
            return null;
        }
        return decrypt(cipher, cipherText);
    }

    public static Cipher getEncryptCipher(String key, String salt, AesEnum aesEnum) {
        //缓存Cipher，避免每次初始化耗时
        return getCipher(key, salt, aesEnum, encryptCipherCache, Cipher.ENCRYPT_MODE);
    }

    public static Cipher getDecryptCipher(String key, String salt, AesEnum aesEnum) {
        //缓存Cipher，避免每次初始化耗时
        return getCipher(key, salt, aesEnum, decryptCipherCache, Cipher.DECRYPT_MODE);
    }

    /**
     * 功能描述：加密
     *
     * @author gy
     * @version 1.0.0
     */
    public static String encrypt(Cipher cipher, String originalText) {
        if (StringUtils.isBlank(originalText) || cipher == null) {
            return null;
        }
        try {
            byte[] encrypted = cipher.doFinal(getBytesUtf8(originalText));
            return Base64.encodeBase64URLSafeString(encrypted);
        } catch (Exception e) {
            log.error("[AesUtil]encrypt exception:originalText={}.", originalText, e);
        }
        return null;
    }

    /**
     * 功能描述：解密
     *
     * @author gy
     * @version 1.0.0
     */
    public static String decrypt(Cipher cipher, String cipherText) {
        if (StringUtils.isBlank(cipherText) || cipher == null) {
            return null;
        }
        try {
            byte[] original = cipher.doFinal(Base64.decodeBase64(cipherText));
            return newStringUtf8(original);
        } catch (Exception e) {
            log.error("[AesUtil]decrypt exception:cipherText={}.", cipherText, e);
        }
        return null;
    }

    private static Cipher getCipher(String key, String salt, AesEnum aesEnum, Map<String, Cipher> cipherCache,
        int cipherMode) {
        log.debug("[AesUtil]getCipher:key={},salt={},cipherMode={},aesEnum={}", key, salt, cipherMode, aesEnum);
        String cipherCacheKey = wrapCipherCacheKey(key, salt);
        Cipher cipher = cipherCache.get(cipherCacheKey);
        if (cipher == null) {
            try {
                cipher = getCipher(key, salt, aesEnum.getCipherName(), cipherMode);
                cipherCache.put(cipherCacheKey, cipher);
            } catch (Exception e) {
                log.error("[AesUtil]getCipher exception:key={},salt={},cipherMode={},aesEnum={}.", key, salt,
                    cipherMode, aesEnum, e);
            }
        }
        return cipher;
    }

    private static String wrapCipherCacheKey(String key, String salt) {
        return String.join(SPLIT, key, salt);
    }

    private static Cipher getCipher(String key, String salt, String cipherName, int cipherMode)
        throws GeneralSecurityException {
        IvParameterSpec iv = new IvParameterSpec(getBytesUtf8(key));
        SecretKeySpec skeySpec = new SecretKeySpec(getBytesUtf8(salt), AES);
        Cipher cipher = Cipher.getInstance(cipherName);
        cipher.init(cipherMode, skeySpec, iv);
        return cipher;
    }


    @Getter
    @AllArgsConstructor
    public static enum AesEnum {

        /**
         * 有向量加密模式, 加密内容不足8位用余位数补足8位, 如{65,65,65,5,5,5,5,5}或{97,97,97,97,97,97,2,2}; 刚好8位补8位8
         */
        CBC_PKCS5PADDING("AES/CBC/PKCS5PADDING"),

        /**
         * 无向量加密模式, 加密内容不足8位用余位数补足8位, 如{65,65,65,5,5,5,5,5}或{97,97,97,97,97,97,2,2}; 刚好8位补8位8
         */
        ECB_PKCS5PADDING("AES/ECB/PKCS5Padding");

        private final String cipherName;
    }

}