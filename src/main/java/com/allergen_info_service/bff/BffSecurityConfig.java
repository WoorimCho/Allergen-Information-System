package com.allergen_info_service.bff;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Auth for the BFF, decision: <b>BFF-enforced session</b>.
 *
 * <p>Only {@code /bff/**} is protected. A {@code POST /bff/login}
 * (form params {@code username} + {@code password}) is checked against the User
 * service by {@link UserAuthenticationProvider}; on success a session cookie is
 * issued and the principal is a {@link BffPrincipal}. Everything else — the
 * legacy Thymeleaf UI, the GoodNight tester — stays open, because the monolith
 * is kept intact until the Phase 3 cutover.
 *
 * <p>CSRF is disabled (this is a JSON API surface; a browser-form login flow
 * would keep it). When services later need to verify the caller themselves, the
 * BFF forwards identity as a header/token — this class is the swap point if the
 * mechanism ever moves to OAuth2.
 */
@Configuration
class BffSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain bffChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/bff/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .formLogin(form -> form
                        .loginProcessingUrl("/bff/login")
                        .successHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT))
                        .failureHandler((req, res, ex) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                .logout(out -> out
                        .logoutUrl("/bff/logout")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (req, res, e) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /** Everything outside /bff/** — unchanged behaviour for the legacy monolith. */
    @Bean
    @Order(2)
    SecurityFilterChain openChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
