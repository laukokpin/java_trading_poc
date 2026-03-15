package com.kp.trading.producer;

import com.kp.trading.event.QuoteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryQuoteEventProducer implements QuoteEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryQuoteEventProducer.class);

    @Override
    public void publish(QuoteEvent event) {
        LOGGER.info("Publishing quote event: {}", event);
    }
}
