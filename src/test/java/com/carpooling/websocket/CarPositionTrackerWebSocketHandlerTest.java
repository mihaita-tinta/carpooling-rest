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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(properties =  {"logging.level.org.springframework.web=TRACE"},
webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarPositionTrackerWebSocketHandlerTest {
    private static final Logger log = LoggerFactory.getLogger(CarPositionTrackerWebSocketHandlerTest.class);
    @LocalServerPort
    int port;

    @Autowired
    TokenProvider tokenProvider;

    private String getAccessToken() {
        Collection<? extends GrantedAuthority> authorities =
                Stream.of("USER", "ADMIN")
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User("junitus", "password", authorities);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, "pass", authorities);
        return "Bearer " + tokenProvider.createToken(authentication);
    }

    @Test
    public void test() {
        WebSocketClient client = new ReactorNettyWebSocketClient();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, getAccessToken());
        client.execute(
                URI.create("ws://localhost:" + port + "/api/websocket"),
                headers,
                session -> {
                    Flux<WebSocketMessage> output = session.receive()
                            .doOnNext(message -> {
                               log.info("test - message: " + message.getPayloadAsText() );
                            })
                            .map(value -> session.textMessage("Echo " + value));

                    return session.send(output);
                })
                .block(Duration.ofSeconds(10L));
    }
}