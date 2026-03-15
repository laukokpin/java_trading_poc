package com.kp.trading.consumer;

import com.kp.trading.event.ExecutionEvent;
import com.kp.trading.event.OrderEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryOrderEventConsumer implements OrderEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryOrderEventConsumer.class);
    private static final BigDecimal DEFAULT_EXECUTION_PRICE = new BigDecimal("1.0000");

    @Override
    public ExecutionEvent consume(OrderEvent event) {
        LOGGER.info("Consuming order event: {}", event);
        return new ExecutionEvent(
                UUID.randomUUID().toString(),
                event.orderId(),
                event.symbol(),
                event.quantity(),
                DEFAULT_EXECUTION_PRICE,
                Instant.now());
    }
}
