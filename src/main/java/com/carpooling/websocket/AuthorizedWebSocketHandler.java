package com.carpooling.websocket;

import org.springframework.security.core.Authentication;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.security.Principal;

public abstract class AuthorizedWebSocketHandler implements WebSocketHandler {

    @Override
    public final Mono<Void> handle(WebSocketSession session) {
        return session.getHandshakeInfo().getPrincipal()
                .filter(this::isAuthorized)
                .flatMap(principal -> doHandle(principal, session));
    }

    private boolean isAuthorized(Principal principal) {
        Authentication authentication = (Authentication) principal;
        return authentication.isAuthenticated() &&
                authentication.getAuthorities().contains("ROLE_USER");
    }

    abstract protected Mono<Void> doHandle(Principal principal, WebSocketSession session);
}
