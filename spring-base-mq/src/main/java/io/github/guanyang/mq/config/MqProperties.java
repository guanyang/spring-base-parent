package io.github.guanyang.mq.config;

import lombok.Data;
import lombok.experimental.Accessors;
import io.github.guanyang.mq.config.support.RocketMQProperties;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Listener;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.kafka.listener.ContainerProperties.AckMode;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = MqProperties.PREFIX)
public class MqProperties {
    public static final String PREFIX = "spring.base-mq";
    public static final String ROCKET_PREFIX = PREFIX + ".rocketmq";
    public static final String KAFKA_PREFIX = PREFIX + ".kafka";
    /**
     * 全局配置
     */
    @NestedConfigurationProperty
    private GlobalConfig globalConfig = new GlobalConfig();
    /**
     * rocketMQ配置
     */
    private Map<String, RocketMQProperties> rocketmq = new LinkedHashMap<>();
    /**
     * kafka配置
     */
    private Map<String, KafkaProperty> kafka = new LinkedHashMap<>();

    @Data
    @Accessors(chain = true)
    public static class GlobalConfig implements Serializable {
        private static final long serialVersionUID = -8461902558701769728L;
        public static final int MAX_RETRY_TIMES = 10;
        public static final int DEFAULT_RETRY_TIMES = 5;
        public static final long DEFAULT_EXPIRE_TIME = 7200000L;
        public static final String IDEMPOTENT_KEY_PREFIX = "default:mq:idempotentKey";
        public static final long DEFAULT_INITIAL_INTERVAL = 2000L;
        public static final double DEFAULT_MULTIPLIER = 1.5;
        public static final long DEFAULT_MAX_INTERVAL = 10000L;
        /**
         * 消息幂等性过期时间，单位毫秒，默认2小时
         */
        private long idempotentExpireMillis = DEFAULT_EXPIRE_TIME;
        /**
         * 重试次数，默认5次
         */
        private int retryTimes = DEFAULT_RETRY_TIMES;

        /**
         * 幂等性key前缀，默认为default:mq:idempotentKey
         */
        private String idempotentKeyPrefix = IDEMPOTENT_KEY_PREFIX;

        /**
         * 重试间隔时间，默认2000毫秒
         */
        private long initialInterval = DEFAULT_INITIAL_INTERVAL;
        /**
         * 重试间隔时间倍数，默认1.5倍
         */
        private double multiplier = DEFAULT_MULTIPLIER;

        /**
         * 重试最大间隔时间，默认10000毫秒
         */
        private long maxInterval = DEFAULT_MAX_INTERVAL;

        public int getRetryTimes() {
            return retryTimes > 0 ? Math.min(retryTimes, MAX_RETRY_TIMES) : DEFAULT_RETRY_TIMES;
        }

        public long getIdempotentExpireMillis() {
            return idempotentExpireMillis > 0 ? idempotentExpireMillis : DEFAULT_EXPIRE_TIME;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class KafkaProperty extends KafkaProperties {
        /**
         * topic
         */
        private String topic;
        /**
         * Consumer listener
         */
        @NestedConfigurationProperty
        private ConsumerListener listener = new ConsumerListener();
    }

    @Data
    @Accessors(chain = true)
    public static class ConsumerListener extends Listener {
        /**
         * Message listener bean name
         *
         * @see org.springframework.kafka.listener.AcknowledgingMessageListener
         */
        private String listenerBeanName;

        public ConsumerListener() {
            this.setAckMode(AckMode.MANUAL_IMMEDIATE);
        }
    }
}
