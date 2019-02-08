package com.carpooling.rest;

import com.carpooling.repository.UserRepository;
import com.carpooling.rest.model.LoginRequest;
import com.carpooling.security.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/authentication")
public class AuthenticationController {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthenticationController(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/")
    public Mono<Map<String, Object>> getAuthentication(@AuthenticationPrincipal Mono<UserDetails> user) {
        return user
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("username", u.getUsername());
                    map.put("roles", AuthorityUtils.authorityListToSet(u

                            .getAuthorities()));
                    return map;
                });
    }


    @PostMapping(value = "/")
    public Mono<ResponseEntity<?>> login(@RequestBody LoginRequest ar) {
        return userRepository.findByUsername(ar.getUsername()).map((user) -> {
            if (passwordEncoder.matches(ar.getPassword(), user.getPassword())) {

                Collection<? extends GrantedAuthority> authorities =
                        user.getRoles().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                User principal = new User("username", "password", authorities);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,
                        null, authorities);

                String token = tokenProvider.createToken(authentication);
                Map<String, Object> map = new HashMap<>();
                map.put("token", token);
                return ResponseEntity.ok(map);
            } else {
                log.debug("login - invalid attempt due to wrong password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }).defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

}
