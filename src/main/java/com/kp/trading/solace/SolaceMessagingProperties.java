package com.kp.trading.solace;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trading.solace")
public class SolaceMessagingProperties {

    private String host = "localhost:55555";
    private String vpnName = "default";
    private String username = "admin";
    private String password = "admin";
    private int connectionRetries = 20;
    private long retryDelayMillis = 3000;
    private String quoteTopic = SolaceTopics.QUOTES;
    private String orderTopic = SolaceTopics.ORDERS;
    private String executionTopic = SolaceTopics.EXECUTIONS;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getVpnName() {
        return vpnName;
    }

    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectionRetries() {
        return connectionRetries;
    }

    public void setConnectionRetries(int connectionRetries) {
        this.connectionRetries = connectionRetries;
    }

    public long getRetryDelayMillis() {
        return retryDelayMillis;
    }

    public void setRetryDelayMillis(long retryDelayMillis) {
        this.retryDelayMillis = retryDelayMillis;
    }

    public String getQuoteTopic() {
        return quoteTopic;
    }

    public void setQuoteTopic(String quoteTopic) {
        this.quoteTopic = quoteTopic;
    }

    public String getOrderTopic() {
        return orderTopic;
    }

    public void setOrderTopic(String orderTopic) {
        this.orderTopic = orderTopic;
    }

    public String getExecutionTopic() {
        return executionTopic;
    }

    public void setExecutionTopic(String executionTopic) {
        this.executionTopic = executionTopic;
    }
}
