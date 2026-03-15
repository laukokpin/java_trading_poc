package com.kp.trading.event;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteEvent(String symbol, BigDecimal bid, BigDecimal ask, Instant timestamp) {
}
