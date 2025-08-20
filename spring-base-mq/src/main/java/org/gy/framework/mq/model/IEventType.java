package org.gy.framework.mq.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gy.framework.core.annotation.CommonService;
import org.gy.framework.core.support.CommonServiceAction;
import org.gy.framework.core.support.CommonServiceManager;

import static org.gy.framework.mq.model.IEventType.EventTypeCode.DEFAULT_DEMO;

/**
 * 功能描述：事件类型定义
 *
 * @version 1.0.0
 */
public interface IEventType extends CommonServiceAction {

    /**
     * 获取事件标识
     */
    String getCode();

    /**
     * 获取事件描述
     */
    String getDesc();

    default void init() {
        CommonServiceManager.registerInstance(IEventType.class, this, IEventType::getCode, true);
    }


    @Getter
    @AllArgsConstructor
    @CommonService
    enum DefaultEventType implements IEventType {

        DEMO_EVENT(DEFAULT_DEMO, "示例事件");
        /**
         * 标识
         */
        private final String code;
        /**
         * 描述
         */
        private final String desc;

    }

    interface EventTypeCode {
        String DEFAULT_DEMO = "DEMO_EVENT";
    }
}
