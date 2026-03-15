package com.kp.trading.producer;

import com.kp.trading.event.QuoteEvent;
import com.kp.trading.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Primary
@Component
public class KafkaQuoteEventProducer implements QuoteEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaQuoteEventProducer.class);

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public KafkaQuoteEventProducer(KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(QuoteEvent event) {
        LOGGER.info("Kafka: publishing quote {}", event);
        kafkaTemplate.send(KafkaTopics.QUOTES, event.symbol(), event);
    }
}
