package com.kp.trading.producer;

import com.kp.trading.event.QuoteEvent;

public interface QuoteEventProducer {

    void publish(QuoteEvent event);
}
