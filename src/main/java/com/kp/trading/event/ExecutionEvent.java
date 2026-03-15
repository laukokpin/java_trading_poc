package com.kp.trading.event;

import java.math.BigDecimal;
import java.time.Instant;

public record ExecutionEvent(
        String executionId,
        String orderId,
        String symbol,
        BigDecimal quantity,
        BigDecimal price,
        Instant timestamp) {
}
