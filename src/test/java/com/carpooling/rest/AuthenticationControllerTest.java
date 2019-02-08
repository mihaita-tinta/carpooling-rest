package com.carpooling.rest;

import com.carpooling.domain.User;
import com.carpooling.repository.UserRepository;
import com.carpooling.rest.model.LoginRequest;
import com.carpooling.rest.model.UserDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"logging.level.org.springframework.web.reactive.function.client.ExchangeFunctions=TRACE",
        "spring.http.log-request-details=true"})
public class AuthenticationControllerTest {
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private ApplicationContext context;

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    WebTestClient client;

    @Before
    public void before() {
        client = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .filter(documentationConfiguration(restDocumentation))
                .build();
    }

    @After
    public void cleanup() {
        userRepository.deleteAll().block();
    }

    @Test
    public void testLogin() throws Exception {

        User user = new User();
        user.setUsername("junit");
        user.setPassword(passwordEncoder.encode("zzz"));
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        user.setFirstName("mih");
        user.setLastName("tnt");
        user.setCreatedDate(LocalDateTime.now());
        userRepository.save(user).block();

        LoginRequest req = new LoginRequest();
        req.setUsername("junit");
        req.setPassword("zzz");
        client.post()
                .uri("/authentication/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(req), LoginRequest.class)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .consumeWith(document("authentication-login"));

    }

    @Test
    public void testLoginFailedDueToWrongPassword() throws Exception {

        User user = new User();
        user.setUsername("junit");
        user.setPassword(passwordEncoder.encode("zzz"));
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        user.setFirstName("mih");
        user.setLastName("tnt");
        user.setCreatedDate(LocalDateTime.now());
        userRepository.save(user).block();

        LoginRequest req = new LoginRequest();
        req.setUsername("junit");
        req.setPassword("yyy");
        client.post()
                .uri("/authentication/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(req), LoginRequest.class)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .consumeWith(document("wrong-password-authentication-login"));

    }

    @Test
    public void testLoginFailedUserDoesntExist() throws Exception {

        LoginRequest req = new LoginRequest();
        req.setUsername("notexistinguser");
        req.setPassword("zzz");
        client.post()
                .uri("/authentication/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(req), LoginRequest.class)
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .consumeWith(document("failed-authentication-login"));

    }


}