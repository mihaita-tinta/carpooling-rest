package com.carpooling.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class TrackingController {

    @Autowired
    @Qualifier("trackingChannel")
    SubscribableChannel channel;

    @GetMapping("/tracking")
    public Mono<Boolean> track(@RequestParam String message) {
        return Mono.just(channel.send(MessageBuilder.withPayload(message).build()));
    }
}
