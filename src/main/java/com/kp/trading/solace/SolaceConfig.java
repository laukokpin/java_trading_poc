package com.kp.trading.solace;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("solace")
@Configuration
@EnableConfigurationProperties(SolaceMessagingProperties.class)
public class SolaceConfig {}
