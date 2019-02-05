package com.carpooling.rest;

import com.carpooling.domain.User;
import com.carpooling.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RequestMapping("/api/users")
@RestController
public class UserController {


    @Autowired
    UserRepository userRepository;

    @GetMapping("/")
    public Flux<User> list() {
        return userRepository.findAll();
    }

    @PostMapping("/")
    public Mono<User> save(@Valid @RequestBody UserDto dto) {

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPassword(dto.getPassword());
        user.setCreatedDate(LocalDateTime.now());
        user.setActive(false);// TODO we need to decide how the users are activated
        return userRepository.save(user);
    }

    @PostMapping("/{id}/activate")
    public Mono<User> save(@PathVariable String id) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    user.setActive(true);
                    return userRepository.save(user);
                });
    }
}
