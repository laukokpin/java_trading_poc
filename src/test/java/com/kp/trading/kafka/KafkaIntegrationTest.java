package com.kp.trading.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.kp.trading.event.ExecutionEvent;
import com.kp.trading.event.OrderEvent;
import com.kp.trading.event.QuoteEvent;
import com.kp.trading.event.Side;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("kafka")
@EmbeddedKafka(
        partitions = 1,
        topics = {KafkaTopics.QUOTES, KafkaTopics.ORDERS, KafkaTopics.EXECUTIONS})
@TestPropertySource(
        properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@DirtiesContext
class KafkaIntegrationTest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        ExecutionCaptureListener executionCaptureListener() {
            return new ExecutionCaptureListener();
        }
    }

    static class ExecutionCaptureListener {

        private final List<ExecutionEvent> received = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = KafkaTopics.EXECUTIONS, groupId = "test-capture-group")
        void capture(ExecutionEvent event) {
            received.add(event);
        }

        List<ExecutionEvent> getReceived() {
            return received;
        }
    }

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private ExecutionCaptureListener captureListener;

    @BeforeEach
    void clearCaptures() {
        captureListener.getReceived().clear();
    }

    @Test
    void orderPublishedToKafkaProducesExecution() {
        OrderEvent order = new OrderEvent(
                "kafka-ord-1", "EURUSD", Side.BUY, new BigDecimal("100000"), Instant.now());

        kafkaTemplate.send(KafkaTopics.ORDERS, order.orderId(), order);

        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !captureListener.getReceived().isEmpty());

        ExecutionEvent execution = captureListener.getReceived().get(0);
        assertThat(execution.orderId()).isEqualTo("kafka-ord-1");
        assertThat(execution.symbol()).isEqualTo("EURUSD");
    }

    @Test
    void quotePublishedToKafkaSucceeds() {
        QuoteEvent quote = new QuoteEvent(
                "GBPUSD", new BigDecimal("1.2700"), new BigDecimal("1.2702"), Instant.now());

        var future = kafkaTemplate.send(KafkaTopics.QUOTES, quote.symbol(), quote);

        await().atMost(5, TimeUnit.SECONDS).until(future::isDone);
        assertThat(future.isCompletedExceptionally()).isFalse();
    }
}
