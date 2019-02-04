package com.carpooling.rest;

import com.carpooling.domain.User;
import com.carpooling.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequestMapping("/users")
@RestController
public class UserController {


    @Autowired
    UserRepository userRepository;

    @GetMapping("/")
    public Flux<User> list() {
        return userRepository.findAll();
    }
}
