package org.gy.framework.sign.dto;

/**
 * @author gy
 */
public interface SignedReq {

    /**
     * 签名信息
     */
    String getSign();

    /**
     * 应用标识
     */
    int getAppId();

    /**
     * 毫秒时间戳
     */
    long getTimestamp();

    /**
     * 请求唯一随机数，建议32位字符串
     */
    String getNonce();

}
