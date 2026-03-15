package com.kp.trading.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TradingFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void quotePublishEndpointAcceptsPayload() throws Exception {
        String quoteJson = """
                {
                  "symbol": "EURUSD",
                  "bid": 1.0810,
                  "ask": 1.0812,
                  "timestamp": "2026-03-15T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/trading/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(quoteJson))
                .andExpect(status().isAccepted());
    }

    @Test
    void orderProcessEndpointReturnsExecution() throws Exception {
        String orderJson = """
                {
                  "orderId": "ord-1",
                  "symbol": "EURUSD",
                  "side": "BUY",
                  "quantity": 100000,
                  "timestamp": "2026-03-15T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/trading/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ord-1"))
                .andExpect(jsonPath("$.symbol").value("EURUSD"));
    }
}
