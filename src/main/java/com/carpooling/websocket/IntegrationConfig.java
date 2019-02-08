package com.carpooling.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.SubscribableChannel;

@Configuration
public class IntegrationConfig {

    @Bean
    public SubscribableChannel trackingChannel() {
        return new PublishSubscribeChannel();
    }
}
