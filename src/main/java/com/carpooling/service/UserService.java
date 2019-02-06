package com.carpooling.service;

import com.carpooling.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserService implements ReactiveUserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User Not Found")))
                .map(user -> {
                    Collection<? extends GrantedAuthority> authorities =
                            user.getRoles()
                                    .stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList());
                    return new User(user.getUsername(), user.getPassword(), authorities);
                });
    }
}
