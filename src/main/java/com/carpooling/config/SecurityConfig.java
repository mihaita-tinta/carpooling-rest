package com.carpooling.config;

import com.carpooling.security.TokenProvider;
import com.carpooling.security.basic.BasicAuthenticationSuccessHandler;
import com.carpooling.security.bearer.BearerTokenReactiveAuthenticationManager;
import com.carpooling.security.bearer.ServerHttpBearerAuthenticationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    ReactiveUserDetailsService userDetailsService;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
                .authorizeExchange()
                .pathMatchers("/login", "/")
                .authenticated()
                .and()
                .addFilterAt(basicAuthenticationFilter(), SecurityWebFiltersOrder.HTTP_BASIC)
                .authorizeExchange()
                .pathMatchers("/api/users/*/activate")
                .hasRole("ADMIN")
                .and()
                .authorizeExchange()
                .pathMatchers("/api/**")
                .authenticated()
                .and()
                .addFilterAt(bearerAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    /**
     * Use the already implemented logic in  AuthenticationWebFilter and set a custom
     * SuccessHandler that will return a JWT when a user is authenticated with user/password
     * Create an AuthenticationManager using the UserDetailsService defined above
     *
     * @return AuthenticationWebFilter
     */
    private AuthenticationWebFilter basicAuthenticationFilter() {
        UserDetailsRepositoryReactiveAuthenticationManager authManager;
        AuthenticationWebFilter basicAuthenticationFilter;
        ServerAuthenticationSuccessHandler successHandler;

        authManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        successHandler = new BasicAuthenticationSuccessHandler(tokenProvider);

        basicAuthenticationFilter = new AuthenticationWebFilter(authManager);
        basicAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);

        return basicAuthenticationFilter;

    }

    /**
     * Use the already implemented logic by AuthenticationWebFilter and set a custom
     * converter that will handle requests containing a Bearer token inside
     * the HTTP Authorization header.
     * Set a dummy authentication manager to this filter, it's not needed because
     * the converter handles this.
     *
     * @return bearerAuthenticationFilter that will authorize requests containing a JWT
     */
    private AuthenticationWebFilter bearerAuthenticationFilter() {
        AuthenticationWebFilter bearerAuthenticationFilter;
        Function<ServerWebExchange, Mono<Authentication>> bearerConverter;
        ReactiveAuthenticationManager authManager;

        authManager = new BearerTokenReactiveAuthenticationManager();
        bearerAuthenticationFilter = new AuthenticationWebFilter(authManager);
        bearerConverter = new ServerHttpBearerAuthenticationConverter(tokenProvider);

        bearerAuthenticationFilter.setAuthenticationConverter(bearerConverter);
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"));

        return bearerAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
