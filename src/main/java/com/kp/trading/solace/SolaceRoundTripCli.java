package com.kp.trading.solace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kp.trading.event.ExecutionEvent;
import com.kp.trading.event.OrderEvent;
import com.kp.trading.event.Side;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class SolaceRoundTripCli {

    private SolaceRoundTripCli() {}

    public static void main(String[] args) throws Exception {
        Arguments parsed = Arguments.fromArgs(args);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ExecutionEvent> executionRef = new AtomicReference<>();

        JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, parsed.host());
        properties.setProperty(JCSMPProperties.VPN_NAME, parsed.vpnName());
        properties.setProperty(JCSMPProperties.USERNAME, parsed.username());
        properties.setProperty(JCSMPProperties.PASSWORD, parsed.password());
        properties.setProperty(JCSMPProperties.CLIENT_NAME, "trading-poc-solace-roundtrip-" + UUID.randomUUID());

        JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();

        Topic executionTopic = JCSMPFactory.onlyInstance().createTopic(parsed.executionTopic());
        Topic orderTopic = JCSMPFactory.onlyInstance().createTopic(parsed.orderTopic());

        XMLMessageConsumer consumer = session.getMessageConsumer(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage message) {
                if (message instanceof TextMessage textMessage) {
                    try {
                        executionRef.set(objectMapper.readValue(textMessage.getText(), ExecutionEvent.class));
                        latch.countDown();
                    } catch (Exception exception) {
                        throw new IllegalStateException("Failed to decode execution message", exception);
                    }
                }
            }

            @Override
            public void onException(JCSMPException exception) {
                throw new IllegalStateException("Solace round-trip consumer failed", exception);
            }
        });
        session.addSubscription(executionTopic);
        consumer.start();

        XMLMessageProducer producer = session.getMessageProducer(new SolacePublishCallback());
        OrderEvent orderEvent = new OrderEvent(
                "solace-roundtrip-001",
                "EURUSD",
                Side.BUY,
                new BigDecimal("100000"),
                Instant.now());
        TextMessage orderMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        orderMessage.setText(objectMapper.writeValueAsString(orderEvent));
        orderMessage.setCorrelationKey(orderEvent.orderId());
        producer.send(orderMessage, orderTopic);

        boolean received = latch.await(parsed.timeoutSeconds(), TimeUnit.SECONDS);

        producer.close();
        consumer.close();
        session.closeSession();

        if (!received || executionRef.get() == null) {
            throw new IllegalStateException("Timed out waiting for execution event on Solace");
        }

        ExecutionEvent execution = executionRef.get();
        System.out.printf(
                Locale.US,
                "executionId=%s orderId=%s symbol=%s quantity=%s price=%s%n",
                execution.executionId(),
                execution.orderId(),
                execution.symbol(),
                execution.quantity(),
                execution.price());
    }

    record Arguments(
            String host,
            String vpnName,
            String username,
            String password,
            String orderTopic,
            String executionTopic,
            long timeoutSeconds) {

        static Arguments fromArgs(String[] args) {
            String host = "localhost:55555";
            String vpnName = "default";
            String username = "admin";
            String password = "admin";
            String orderTopic = SolaceTopics.ORDERS;
            String executionTopic = SolaceTopics.EXECUTIONS;
            long timeoutSeconds = 15;

            for (int index = 0; index < args.length; index++) {
                String arg = args[index];
                if ("--host".equals(arg) && index + 1 < args.length) {
                    host = args[++index];
                } else if ("--vpn".equals(arg) && index + 1 < args.length) {
                    vpnName = args[++index];
                } else if ("--username".equals(arg) && index + 1 < args.length) {
                    username = args[++index];
                } else if ("--password".equals(arg) && index + 1 < args.length) {
                    password = args[++index];
                } else if ("--order-topic".equals(arg) && index + 1 < args.length) {
                    orderTopic = args[++index];
                } else if ("--execution-topic".equals(arg) && index + 1 < args.length) {
                    executionTopic = args[++index];
                } else if ("--timeout-seconds".equals(arg) && index + 1 < args.length) {
                    timeoutSeconds = Long.parseLong(args[++index]);
                } else {
                    throw new IllegalArgumentException("Unsupported argument: " + arg);
                }
            }

            return new Arguments(host, vpnName, username, password, orderTopic, executionTopic, timeoutSeconds);
        }
    }
}
