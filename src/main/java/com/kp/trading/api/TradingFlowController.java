package com.kp.trading.api;

import com.kp.trading.event.ExecutionEvent;
import com.kp.trading.event.OrderEvent;
import com.kp.trading.event.QuoteEvent;
import com.kp.trading.service.TradingFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trading")
public class TradingFlowController {

    private final TradingFlowService tradingFlowService;

    public TradingFlowController(TradingFlowService tradingFlowService) {
        this.tradingFlowService = tradingFlowService;
    }

    @PostMapping("/quotes")
    public ResponseEntity<Void> publishQuote(@RequestBody QuoteEvent event) {
        tradingFlowService.publishQuote(event);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/orders")
    public ResponseEntity<ExecutionEvent> processOrder(@RequestBody OrderEvent event) {
        return ResponseEntity.ok(tradingFlowService.processOrder(event));
    }
}
