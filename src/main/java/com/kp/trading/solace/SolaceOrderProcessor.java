package com.kp.trading.solace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kp.trading.event.ExecutionEvent;
import com.kp.trading.event.OrderEvent;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("solace")
@Component
public class SolaceOrderProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolaceOrderProcessor.class);
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("1.0000");

    private final ObjectMapper objectMapper;
    private final SolaceSessionFactory sessionFactory;
    private final SolaceMessagingProperties properties;

    private JCSMPSession session;
    private XMLMessageConsumer consumer;
    private XMLMessageProducer producer;
    private Topic executionTopic;

    public SolaceOrderProcessor(
            ObjectMapper objectMapper,
            SolaceSessionFactory sessionFactory,
            SolaceMessagingProperties properties) {
        this.objectMapper = objectMapper;
        this.sessionFactory = sessionFactory;
        this.properties = properties;
    }

    @PostConstruct
    void start() throws JCSMPException {
        session = sessionFactory.createConnectedSession("trading-poc-solace-order-processor");
        executionTopic = JCSMPFactory.onlyInstance().createTopic(properties.getExecutionTopic());
        producer = session.getMessageProducer(new SolacePublishCallback());
        consumer = session.getMessageConsumer(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage message) {
                if (message instanceof TextMessage textMessage) {
                    handleOrderMessage(textMessage);
                }
            }

            @Override
            public void onException(JCSMPException exception) {
                LOGGER.error("Solace order processor consumer failure", exception);
            }
        });

        session.addSubscription(JCSMPFactory.onlyInstance().createTopic(properties.getOrderTopic()));
        consumer.start();
    }

    private void handleOrderMessage(TextMessage textMessage) {
        try {
            OrderEvent event = objectMapper.readValue(textMessage.getText(), OrderEvent.class);
            LOGGER.info("Solace: received order {}", event);

            ExecutionEvent execution = new ExecutionEvent(
                    UUID.randomUUID().toString(),
                    event.orderId(),
                    event.symbol(),
                    event.quantity(),
                    DEFAULT_PRICE,
                    Instant.now());

            TextMessage executionMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            executionMessage.setText(objectMapper.writeValueAsString(execution));
            executionMessage.setCorrelationKey(execution.orderId());
            producer.send(executionMessage, executionTopic);
            LOGGER.info("Solace: published execution {}", execution.executionId());
        } catch (JsonProcessingException | JCSMPException exception) {
            throw new IllegalStateException("Failed to process Solace order message", exception);
        }
    }

    @PreDestroy
    void stop() {
        if (consumer != null) {
            consumer.close();
        }
        if (producer != null) {
            producer.close();
        }
        if (session != null) {
            session.closeSession();
        }
    }
}
