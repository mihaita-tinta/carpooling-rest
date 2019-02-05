package com.carpooling.rest;

import com.carpooling.domain.User;
import com.carpooling.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"logging.level.org.springframework.web.reactive.function.client.ExchangeFunctions=TRACE",
        "spring.http.log-request-details=true"})
public class UserControllerTest {
    @Autowired
    private ApplicationContext context;

    @Autowired
    UserRepository userRepository;

    WebTestClient client;

    @Before
    public void before() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    @WithMockUser
    public void testList() throws Exception {
        User user = new User();
        user.setUsername("junit");
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        user.setPassword("xxx");
        user.setFirstName("mih");
        user.setLastName("tnt");
        user.setCreatedDate(LocalDateTime.now());
        userRepository.save(user).block();

        client.get()
                .uri("/api/users/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(s -> System.out.println(new String(s.getResponseBody())))
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$.[?(@.username == 'junit')]").exists();
    }

    @After
    public void cleanup() {
        userRepository.deleteAll().block();
    }

    @Test
    @WithMockUser
    public void testSave() throws Exception {
        UserDto user = new UserDto();
        user.setFirstName("f1");
        user.setLastName("f2");
        user.setUsername("junit");
        user.setPassword("zzz");
        client.mutateWith(csrf())
                .post()
                .uri("/api/users/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(user), UserDto.class)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("f1")
                .jsonPath("$.lastName").isEqualTo("f2")
                .jsonPath("$.createdDate").isNotEmpty()
                .jsonPath("$.id").isNotEmpty();

    }

    @Test
    @WithMockUser
    public void testSaveNotValid() throws Exception {
        UserDto user = new UserDto();
        client.mutateWith(csrf())
                .post()
                .uri("/api/users/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(user), UserDto.class)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody()
                .consumeWith(s -> System.out.println(new String(s.getResponseBody())))
                .jsonPath("$.errors[?(@.codes[0] == 'NotNull.userDto.firstName')]").exists()
                .jsonPath("$.errors[?(@.codes[0] == 'NotNull.userDto.lastName')]").exists();

    }
}