package org.gy.framework.mq.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gy.framework.core.annotation.CommonService;
import org.gy.framework.core.support.CommonServiceAction;
import org.gy.framework.core.support.CommonServiceManager;

import static org.gy.framework.mq.model.IMessageType.MessageTypeCode.DEFAULT_NORMAL;
import static org.gy.framework.mq.model.IMessageType.MessageTypeCode.DEFAULT_ORDERLY;

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

    default void init() {
        CommonServiceManager.registerInstance(IMessageType.class, this, IMessageType::getCode);
    }


    @Getter
    @AllArgsConstructor
    @CommonService
    enum DefaultMessageType implements IMessageType {

        NORMAL(DEFAULT_NORMAL, "普通消息"),

        ORDERLY(DEFAULT_ORDERLY, "顺序消息");
        /**
         * 标识
         */
        private final String code;
        /**
         * 描述
         */
        private final String desc;

    }

    interface MessageTypeCode {
        String DEFAULT_NORMAL = "normal";
        String DEFAULT_ORDERLY = "orderly";
    }
}
