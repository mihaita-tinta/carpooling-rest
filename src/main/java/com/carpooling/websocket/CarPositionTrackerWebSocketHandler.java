package com.carpooling.websocket;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static java.time.Duration.ofSeconds;

@Component
public class CarPositionTrackerWebSocketHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(CarPositionTrackerWebSocketHandler.class);

    private final Map<String, MessageHandler> connections = new ConcurrentHashMap<>();
    private final SubscribableChannel channel;

    @Autowired
    public CarPositionTrackerWebSocketHandler(@Qualifier("trackingChannel") SubscribableChannel channel) {
        this.channel = channel;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        String sessionId = session.getId();

        Publisher<WebSocketMessage> delayedMessages = Flux
                .create((Consumer<FluxSink<WebSocketMessage>>) sink -> {
                    MessageHandler handler = new ForwardingMessageHandler(sink, session);
                    connections.put(sessionId, handler);
                    channel.subscribe(handler);
                })
                .onErrorResume(Exception.class, Flux::error)
                .doOnComplete(() -> log.info("goodbye!"))
                .doFinally(signalType -> {
                    log.info("handle - finally for " + sessionId);
                    channel.unsubscribe(connections.get(sessionId));
                    connections.remove(sessionId);
                });
        return session.send(delayedMessages);
    }

}