package com.carpooling.security;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class TokenProviderTest {
    private static final Logger log = LoggerFactory.getLogger(TokenProviderTest.class);
    TokenProvider tokenProvider;

    @Before
    public void before() {
        tokenProvider = new TokenProvider();
        tokenProvider.init();
    }

    @Test
    public void test() {

        Collection<? extends GrantedAuthority> authorities =
                Stream.of("USER", "ADMIN")
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User("username", "password", authorities);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, "pass", authorities);
        String token = tokenProvider.createToken(authentication);

        boolean isValid = tokenProvider.validateToken(token);

        assertEquals(true, isValid);

        Authentication fromJwt = tokenProvider.getAuthentication(token);
        assertEquals(principal.getUsername(), fromJwt.getName());
        log.debug("Run: \n" +
                "curl -v --header \"Authorization:Bearer " + token + "\" http://localhost:8080/api/users/");

    }
}