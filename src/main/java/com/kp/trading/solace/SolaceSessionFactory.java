package com.kp.trading.solace;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("solace")
@Component
public class SolaceSessionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolaceSessionFactory.class);

    private final SolaceMessagingProperties properties;

    public SolaceSessionFactory(SolaceMessagingProperties properties) {
        this.properties = properties;
    }

    public JCSMPSession createConnectedSession(String clientName) throws JCSMPException {
        JCSMPException lastException = null;
        for (int attempt = 1; attempt <= properties.getConnectionRetries(); attempt++) {
            try {
                JCSMPProperties sessionProperties = new JCSMPProperties();
                sessionProperties.setProperty(JCSMPProperties.HOST, properties.getHost());
                sessionProperties.setProperty(JCSMPProperties.VPN_NAME, properties.getVpnName());
                sessionProperties.setProperty(JCSMPProperties.USERNAME, properties.getUsername());
                sessionProperties.setProperty(JCSMPProperties.PASSWORD, properties.getPassword());
                sessionProperties.setProperty(JCSMPProperties.CLIENT_NAME, clientName);

                JCSMPSession session = JCSMPFactory.onlyInstance().createSession(sessionProperties);
                session.connect();
                return session;
            } catch (JCSMPException exception) {
                lastException = exception;
                LOGGER.warn(
                        "Solace connection attempt {} of {} failed for client {}: {}",
                        attempt,
                        properties.getConnectionRetries(),
                        clientName,
                        exception.getMessage());
                sleepBeforeRetry();
            }
        }

        throw lastException;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(properties.getRetryDelayMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry Solace connection", exception);
        }
    }
}
