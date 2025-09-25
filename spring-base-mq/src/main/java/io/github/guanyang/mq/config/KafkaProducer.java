package io.github.guanyang.mq.config;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import io.github.guanyang.mq.config.MqProperties.KafkaProperty;
import io.github.guanyang.mq.config.support.KafkaProducerContextInterceptor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Producer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Slf4j
public class KafkaProducer implements InitializingBean, DisposableBean {

    private final KafkaProperty kafkaProperty;

    private final String topic;

    private KafkaTemplate<Object, Object> producer;

    public KafkaProducer(final KafkaProperty kafkaProperty) {
        this.kafkaProperty = checkConfig(kafkaProperty);
        this.topic = kafkaProperty.getTopic();
    }

    protected KafkaProperty checkConfig(final KafkaProperty properties) {
        List<String> bootstrapServers = properties.getBootstrapServers();
        Assert.notEmpty(bootstrapServers, () -> "KafkaProperty.bootstrapServers must not be null");

        String topicName = properties.getTopic();
        Assert.hasText(topicName, () -> "KafkaProperty.topic must not be null");

        Producer producer = properties.getProducer();
        Assert.notNull(producer, () -> "KafkaProperty.producer must not be null");

        return properties;
    }

    protected KafkaTemplate<Object, Object> initProducer() {
        ProducerFactory<Object, Object> producerFactory = kafkaProducerFactory(kafkaProperty);
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        if (Objects.nonNull(kafkaProperty.getTemplate())) {
            kafkaTemplate.setDefaultTopic(kafkaProperty.getTemplate().getDefaultTopic());
        }
        return kafkaTemplate;
    }

    protected ProducerFactory<Object, Object> kafkaProducerFactory(KafkaProperty kafkaProperty) {
        Map<String, Object> properties = kafkaProperty.buildProducerProperties();
        KafkaProducerContextInterceptor.addInterceptor(properties, KafkaProducerContextInterceptor.class);
        DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(properties);
        String transactionIdPrefix = kafkaProperty.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        Map<String, DefaultKafkaProducerFactoryCustomizer> customizerMap = SpringUtil.getBeansOfType(DefaultKafkaProducerFactoryCustomizer.class);
        customizerMap.values().forEach(customizer -> customizer.customize(factory));
        return factory;
    }

    @Override
    public void destroy() throws Exception {
        if (producer != null) {
            log.info("kafka生产者服务关闭开始: {}", topic);
            producer.destroy();
            log.info("kafka生产者服务关闭成功: {}", topic);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("kafka生产者启动开始: {}", topic);
        producer = initProducer();
        log.info("kafka生产者启动成功: {}", topic);
    }

}
