package com.kp.trading.solace;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolacePublishCallback implements JCSMPStreamingPublishCorrelatingEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolacePublishCallback.class);

    @Override
    public void responseReceivedEx(Object correlationKey) {
        LOGGER.debug("Solace publish acknowledged for correlation key {}", correlationKey);
    }

    @Override
    public void handleErrorEx(Object correlationKey, JCSMPException cause, long timestamp) {
        LOGGER.error("Solace publish failed for correlation key {}", correlationKey, cause);
    }
}
