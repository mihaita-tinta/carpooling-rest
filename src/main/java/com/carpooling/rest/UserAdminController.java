package com.carpooling.rest;

import com.carpooling.domain.User;
import com.carpooling.repository.UserRepository;
import com.carpooling.rest.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RequestMapping("/api/users")
@RestController
public class UserAdminController {


    @Autowired
    UserRepository userRepository;

    @PostMapping("/{id}/activate")
    public Mono<User> save(@PathVariable String id) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    user.setActive(true);
                    return userRepository.save(user);
                });
    }
}
