package com.carpooling.rest;

import com.carpooling.domain.User;
import com.carpooling.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequestMapping("/users")
@RestController
public class UserController {


    @Autowired
    UserRepository userRepository;

    @GetMapping("/")
    public Flux<User> list() {
        return userRepository.findAll();
    }

    @PostMapping("/")
    public Mono<User> save(@RequestBody User user) {
        if (user.getId() == null)
            user.setCreatedDate(LocalDateTime.now());
        return userRepository.save(user);
    }
}
