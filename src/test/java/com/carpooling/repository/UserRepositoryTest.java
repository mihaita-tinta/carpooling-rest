package com.carpooling.repository;

import com.carpooling.domain.User;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataMongoTest
public class UserRepositoryTest {

    @Autowired
    UserRepository repository;

    @Test
    public void test() {
        User user = new User();
        user.setFirstName("user1");
        user.setLastName("last name");
        user.setUsername("junit");
        user.setPassword("pazz");
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        user.setCreatedDate(LocalDateTime.now());

        Mono<User> userMono = repository.save(user);

        StepVerifier
                .create(userMono)
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertNotNull(saved.getCreatedDate());
                })
                .expectComplete()
                .verify();

        Mono<User> findMono = repository.findById(user.getId());
        StepVerifier
                .create(findMono)
                .assertNext(m -> {
                    assertEquals(user.getFirstName(), m.getFirstName());
                    assertEquals(user.getId(), m.getId());
                })
                .expectComplete()
                .verify();

        repository.deleteById(user.getId())
                    .block();
    }

    @Test
    public void testFindByUsername() {
        User user = new User();
        user.setFirstName("user1");
        user.setLastName("last name");
        user.setUsername("junit");
        user.setPassword("pazz");
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        user.setCreatedDate(LocalDateTime.now());

        repository.save(user).block();

        Mono<User> findMono = repository.findByUsername("junit");
        StepVerifier
                .create(findMono)
                .assertNext(m -> {
                    assertEquals(user.getUsername(), m.getUsername());
                    assertEquals(user.getId(), m.getId());
                    assertArrayEquals(user.getRoles().toArray(), m.getRoles().toArray());
                })
                .expectComplete()
                .verify();

    }

    @After
    public void cleanup() {
        repository.deleteAll().block();
    }
}