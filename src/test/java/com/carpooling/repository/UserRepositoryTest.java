package com.carpooling.repository;

import com.carpooling.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
        Mono<User> userMono = repository.save(user);

        StepVerifier
                .create(userMono)
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
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
}