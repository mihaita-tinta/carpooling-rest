package com.carpooling.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static java.time.Duration.ofSeconds;

@Component
public class CarPositionTrackerWebSocketHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(CarPositionTrackerWebSocketHandler.class);
    private static final ObjectMapper json = new ObjectMapper();

    private Flux<String> eventFlux = Flux.generate(sink -> {
        Event event = new Event(UUID.randomUUID().toString(), Instant.now().toString());
        try {
            String val = json.writeValueAsString(event);
            log.info("generate: " + val );
            sink.next(val);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            sink.error(e);
        }
    });


    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {

        return webSocketSession.send(eventFlux
                                        .map(s -> {
                                            log.info("send - s: " + s );
                                            return webSocketSession.textMessage(s);
                                        })
                                        .delayElements(Duration.ofSeconds(1))
                                        .doOnError(t -> {
                                            log.info("doOnComplete - eventFlux error: " + t);
                                        })
                                        .doFinally(s -> {
                                            log.info("doOnComplete - eventFlux signal: " + s);
                                        })
                    )
                .and(webSocketSession.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .flatMap(message -> {
                            log.info("received - message: " + message);
                            return toMessageWithPrincipal(message, webSocketSession);
                        })
                        .doOnNext(m -> {
                            log.info("received - message: " + m);
                        })
                        .log()
                        .doFinally(s -> {
                            log.info("doOnComplete - signal: " + s);
                        }));
    }
    Mono<?> toMessageWithPrincipal(String message, WebSocketSession user) {
        return user.getHandshakeInfo().getPrincipal()
                .map(principal ->
                        MessageBuilder
                                .withPayload(message)
                                .setHeader("user",
                                        principal.getName())
                                .build());
    }
    public static class Event {
        private String eventId;
        private String eventDt;

        public Event(String eventId, String eventDt) {
            this.eventId = eventId;
            this.eventDt = eventDt;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public String getEventDt() {
            return eventDt;
        }

        public void setEventDt(String eventDt) {
            this.eventDt = eventDt;
        }
    }
}