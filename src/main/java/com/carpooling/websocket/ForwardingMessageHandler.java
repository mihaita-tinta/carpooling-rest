package com.carpooling.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

public class ForwardingMessageHandler implements MessageHandler {

    private final FluxSink<WebSocketMessage> sink;
    private final WebSocketSession wsSession;

    ForwardingMessageHandler(FluxSink<WebSocketMessage> sink, WebSocketSession wsh) {
        this.sink = sink;
        this.wsSession = wsh;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {

        String strPayloadFromChannel = String.class.cast(message.getPayload());

        WebSocketMessage wsMsg = wsSession.textMessage(strPayloadFromChannel);
        sink.next(wsMsg);
    }
}