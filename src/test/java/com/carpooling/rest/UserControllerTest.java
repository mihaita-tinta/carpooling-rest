package com.carpooling.rest;

import com.carpooling.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"logging.level.org.springframework.web.reactive.function.client.ExchangeFunctions=TRACE",
        "spring.http.log-request-details=true"})
public class UserControllerTest {
    @Autowired
    private ApplicationContext context;

    WebTestClient client;

    @Before
    public void before() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    @WithMockUser
    public void testList() throws Exception {
        client.get()
                .uri("/users/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.length", 0);
    }

    @Test
    @WithMockUser
    public void testSave() throws Exception {
        User user = new User();
        user.setFirstName("f1");
        user.setLastName("f2");
        client.mutateWith(csrf())
                .post()
                .uri("/users/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(user), User.class)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("f1")
                .jsonPath("$.lastName").isEqualTo("f2")
                .jsonPath("$.createdDate").isNotEmpty()
                .jsonPath("$.id").isNotEmpty();

    }
}