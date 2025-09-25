package io.github.guanyang.sign.dto;

import io.github.guanyang.sign.util.ParamSignUtils;

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
     * 请求唯一随机数，建议32位字符串，可参考：{@link ParamSignUtils#uuid()}
     */
    String getNonce();

}
