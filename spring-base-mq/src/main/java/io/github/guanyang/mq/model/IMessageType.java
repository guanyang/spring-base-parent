package io.github.guanyang.mq.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.github.guanyang.core.annotation.CommonService;
import io.github.guanyang.core.support.CommonServiceAction;
import io.github.guanyang.core.support.CommonServiceManager;

import static io.github.guanyang.mq.model.IMessageType.MessageTypeCode.*;
import static io.github.guanyang.mq.model.MqType.KAFKA;
import static io.github.guanyang.mq.model.MqType.ROCKETMQ;

/**
 * 功能描述：消息类型定义
 *
 * @version 1.0.0
 */
public interface IMessageType extends CommonServiceAction {

    /**
     * 获取消息类型标识
     */
    String getCode();

    /**
     * 获取消息类型描述
     */
    String getDesc();

    /**
     * MQ类型
     */
    MqType getMqType();

    default void init() {
        CommonServiceManager.registerInstance(IMessageType.class, this, IMessageType::getCode, true);
    }


    @Getter
    @AllArgsConstructor
    @CommonService
    enum DefaultMessageType implements IMessageType {

        NORMAL(DEFAULT_NORMAL, "普通消息", ROCKETMQ),

        ORDERLY(DEFAULT_ORDERLY, "顺序消息", ROCKETMQ),

        KAFKA_DEFAULT(DEFAULT_KAFKA, "kafka消息", KAFKA);
        /**
         * 标识，必须唯一
         */
        private final String code;
        /**
         * 描述
         */
        private final String desc;
        /**
         * MQ类型
         */
        private final MqType mqType;
    }

    interface MessageTypeCode {
        String DEFAULT_NORMAL = "normal";
        String DEFAULT_ORDERLY = "orderly";
        String DEFAULT_KAFKA = "defaultConfig";
    }
}
