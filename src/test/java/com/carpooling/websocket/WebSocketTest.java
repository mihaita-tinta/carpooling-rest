package com.carpooling.websocket;

import com.carpooling.security.TokenProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WebSocketTest {
    private static final Logger log = LoggerFactory.getLogger(WebSocketTest.class);

    private String getAccessToken() {
        TokenProvider tokenProvider = new TokenProvider();
        tokenProvider.init();

        Collection<? extends GrantedAuthority> authorities =
                Stream.of("USER", "ADMIN")
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User("junitus", "password", authorities);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, "pass", authorities);
        return "Bearer " + tokenProvider.createToken(authentication);
    }

//    @Test
    public void test() {
        WebSocketClient client = new ReactorNettyWebSocketClient();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, getAccessToken());
        client.execute(
                URI.create("ws://localhost:8081/api/websocket"),
                headers,
                session -> {
                    Flux<WebSocketMessage> output = session.receive()
                            .doOnNext(message -> {
                               log.info("test - message: " + message.getPayloadAsText() );
                            })
                            .map(value -> session.textMessage("Echo " + value));

                    return session.send(output);
                })
                .block(Duration.ofSeconds(30L));
    }
}