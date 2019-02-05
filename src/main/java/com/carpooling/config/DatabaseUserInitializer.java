package com.carpooling.config;

import com.carpooling.domain.User;
import com.carpooling.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;

@Configuration
public class DatabaseUserInitializer {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Bean
    public CommandLineRunner databaseInitializer() {
        return args -> {
            userRepository.findByUsername("mihaita.tinta")
                    .switchIfEmpty(addUsers())
            .block();
        };
    }

    Mono<User> addUsers() {
        User user = new User();
        user.setUsername("mihaita.tinta");
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        user.setPassword(encoder.encode("mih"));
        user.setFirstName("mih");
        user.setLastName("tnt");
        user.setCreatedDate(LocalDateTime.now());
        return userRepository.save(user);
    }
}
