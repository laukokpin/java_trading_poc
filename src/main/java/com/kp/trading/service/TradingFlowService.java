package com.kp.trading.service;

import com.kp.trading.consumer.OrderEventConsumer;
import com.kp.trading.event.ExecutionEvent;
import com.kp.trading.event.OrderEvent;
import com.kp.trading.event.QuoteEvent;
import com.kp.trading.producer.QuoteEventProducer;
import org.springframework.stereotype.Service;

@Service
public class TradingFlowService {

    private final QuoteEventProducer quoteEventProducer;
    private final OrderEventConsumer orderEventConsumer;

    public TradingFlowService(QuoteEventProducer quoteEventProducer, OrderEventConsumer orderEventConsumer) {
        this.quoteEventProducer = quoteEventProducer;
        this.orderEventConsumer = orderEventConsumer;
    }

    public void publishQuote(QuoteEvent event) {
        quoteEventProducer.publish(event);
    }

    public ExecutionEvent processOrder(OrderEvent event) {
        return orderEventConsumer.consume(event);
    }
}
