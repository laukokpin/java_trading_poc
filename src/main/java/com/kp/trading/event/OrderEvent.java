package com.kp.trading.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderEvent(String orderId, String symbol, Side side, BigDecimal quantity, Instant timestamp) {
}
