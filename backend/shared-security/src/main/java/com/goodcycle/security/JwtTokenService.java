package com.goodcycle.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

public class JwtTokenService {
    private final JwtEncoder jwtEncoder;
    private final long lifetimeMinutes;

    public JwtTokenService(JwtEncoder jwtEncoder, long lifetimeMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.lifetimeMinutes = lifetimeMinutes;
    }

    public String issue(String phone, String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("goodcycle")
                .issuedAt(now)
                .expiresAt(now.plus(lifetimeMinutes, ChronoUnit.MINUTES))
                .subject(phone)
                .claim("roles", List.of(role))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader.with(MacAlgorithm.HS256).build(), claims
        )).getTokenValue();
    }
}
