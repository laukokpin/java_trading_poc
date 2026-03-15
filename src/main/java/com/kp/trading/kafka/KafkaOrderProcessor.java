package com.kp.trading.kafka;

import com.kp.trading.event.ExecutionEvent;
import com.kp.trading.event.OrderEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
public class KafkaOrderProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOrderProcessor.class);
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("1.0000");

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public KafkaOrderProcessor(KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.ORDERS, groupId = "trading-poc-group")
    public void onOrder(OrderEvent event) {
        LOGGER.info("Kafka: received order {}", event);
        ExecutionEvent execution = new ExecutionEvent(
                UUID.randomUUID().toString(),
                event.orderId(),
                event.symbol(),
                event.quantity(),
                DEFAULT_PRICE,
                Instant.now());
        kafkaTemplate.send(KafkaTopics.EXECUTIONS, execution.orderId(), execution);
        LOGGER.info("Kafka: published execution {}", execution.executionId());
    }
}
