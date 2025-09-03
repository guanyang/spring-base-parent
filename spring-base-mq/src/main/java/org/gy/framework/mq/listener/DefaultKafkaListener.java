package org.gy.framework.mq.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.gy.framework.mq.model.MqType;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
public class DefaultKafkaListener extends AbstractMessageListener implements AcknowledgingMessageListener<String, String> {
    @Override
    public void onMessage(ConsumerRecord<String, String> data, Acknowledgment acknowledgment) {
        long startTime = System.currentTimeMillis();
        messageHandler(MqType.KAFKA, data, this);
        log.info("[DefaultKafkaListener]消费处理成功：topic={},partition={},offset={},time={}ms", data.topic(), data.partition(), data.offset(), (System.currentTimeMillis() - startTime));
        acknowledgment.acknowledge();
    }
}
