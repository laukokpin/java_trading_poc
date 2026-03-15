package com.kp.trading.consumer;

import com.kp.trading.event.ExecutionEvent;
import com.kp.trading.event.OrderEvent;

public interface OrderEventConsumer {

    ExecutionEvent consume(OrderEvent event);
}
