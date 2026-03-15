package com.kp.trading.solace;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("solace")
@Component
public class SolaceQuoteLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolaceQuoteLogger.class);

    private final SolaceSessionFactory sessionFactory;
    private final SolaceMessagingProperties properties;

    private JCSMPSession session;
    private XMLMessageConsumer consumer;

    public SolaceQuoteLogger(SolaceSessionFactory sessionFactory, SolaceMessagingProperties properties) {
        this.sessionFactory = sessionFactory;
        this.properties = properties;
    }

    @PostConstruct
    void start() throws JCSMPException {
        session = sessionFactory.createConnectedSession("trading-poc-solace-quote-logger");
        consumer = session.getMessageConsumer(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage message) {
                if (message instanceof TextMessage textMessage) {
                    LOGGER.info("Solace: received quote {}", textMessage.getText());
                }
            }

            @Override
            public void onException(JCSMPException exception) {
                LOGGER.error("Solace quote logger consumer failure", exception);
            }
        });
        Topic quoteTopic = JCSMPFactory.onlyInstance().createTopic(properties.getQuoteTopic());
        session.addSubscription(quoteTopic);
        consumer.start();
    }

    @PreDestroy
    void stop() {
        if (consumer != null) {
            consumer.close();
        }
        if (session != null) {
            session.closeSession();
        }
    }
}
