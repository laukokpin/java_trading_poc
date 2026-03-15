package com.kp.trading.solace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kp.trading.event.QuoteEvent;
import com.kp.trading.producer.QuoteEventProducer;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("solace")
@Primary
@Component
public class SolaceQuoteEventProducer implements QuoteEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolaceQuoteEventProducer.class);

    private final ObjectMapper objectMapper;
    private final Topic quoteTopic;
    private final JCSMPSession session;
    private final XMLMessageProducer producer;

    public SolaceQuoteEventProducer(
            ObjectMapper objectMapper,
            SolaceSessionFactory sessionFactory,
            SolaceMessagingProperties properties)
            throws JCSMPException {
        this.objectMapper = objectMapper;
        this.quoteTopic = JCSMPFactory.onlyInstance().createTopic(properties.getQuoteTopic());
        this.session = sessionFactory.createConnectedSession("trading-poc-solace-quote-producer");
        this.producer = session.getMessageProducer(new SolacePublishCallback());
    }

    @Override
    public void publish(QuoteEvent event) {
        try {
            TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            message.setText(objectMapper.writeValueAsString(event));
            message.setCorrelationKey(event.symbol());
            producer.send(message, quoteTopic);
            LOGGER.info("Solace: published quote {}", event);
        } catch (JCSMPException | JsonProcessingException exception) {
            throw new IllegalStateException("Failed to publish quote to Solace", exception);
        }
    }

    @PreDestroy
    void shutdown() {
        producer.close();
        session.closeSession();
    }
}
