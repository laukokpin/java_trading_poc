package com.kp.trading.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Profile("kafka")
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic quotesTopic() {
        return TopicBuilder.name(KafkaTopics.QUOTES).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(KafkaTopics.ORDERS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic executionsTopic() {
        return TopicBuilder.name(KafkaTopics.EXECUTIONS).partitions(1).replicas(1).build();
    }
}
