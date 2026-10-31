package com.goodcycle.identity.security;

import com.goodcycle.security.JwtConfig;
import com.goodcycle.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@Import(JwtConfig.class)
public class IdentitySecurityConfig {
    @Bean
    SecurityFilterChain identityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/otp/request",
                                "/api/auth/otp/verify",
                                "/api/auth/local-token",
                                "/actuator/health")
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    JwtTokenService jwtTokenService(JwtEncoder encoder, @Value("${security.jwt.lifetime-minutes:30}") long lifetimeMinutes) {
        return new JwtTokenService(encoder, lifetimeMinutes);
    }
}
