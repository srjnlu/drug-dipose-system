package com.goodcycle.identity.otp;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.goodcycle.identity.user.Customer;
import com.goodcycle.identity.user.CustomerRepository;
import com.goodcycle.security.JwtTokenService;

@Service
public class OtpService {
    private static final Duration OTP_LIFETIME = Duration.ofMinutes(5);
    private static final Duration REQUEST_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    private final ConcurrentHashMap<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final CustomerRepository customers;
    private final SmsSender smsSender;
    private final JwtTokenService tokens;
    private final boolean exposeCode;

    public OtpService(
            CustomerRepository customers,
            SmsSender smsSender,
            JwtTokenService tokens,
            @Value("${otp.expose-code:false}") boolean exposeCode,
            Environment environment) {
        boolean localProfile = java.util.Arrays.asList(environment.getActiveProfiles()).contains("local");
        if (exposeCode && !localProfile) {
            throw new IllegalStateException("OTP codes may only be exposed with the local profile.");
        }
        this.customers = customers;
        this.smsSender = smsSender;
        this.tokens = tokens;
        this.exposeCode = exposeCode;
    }

    public String requestCode(String phone) {
        Instant now = Instant.now();
        Challenge existing = challenges.get(phone);
        if (existing != null && now.isBefore(existing.requestedAt().plus(REQUEST_COOLDOWN))) {
            throw new OtpException("Please wait before requesting another code.");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        smsSender.send(phone, "Your GoodCycle verification code is " + code + ". It expires in 5 minutes.");
        challenges.put(phone, new Challenge(code, now, 0));
        return exposeCode ? code : null;
    }

    public String verify(String phone, String code) {
        Challenge challenge = challenges.get(phone);
        Instant now = Instant.now();
        if (challenge == null || now.isAfter(challenge.requestedAt().plus(OTP_LIFETIME))) {
            challenges.remove(phone);
            throw new OtpException("The verification code has expired. Request a new one.");
        }
        if (challenge.attempts() >= MAX_ATTEMPTS) {
            challenges.remove(phone);
            throw new OtpException("Too many incorrect attempts. Request a new code.");
        }
        if (!challenge.code().equals(code)) {
            challenges.replace(phone, challenge, new Challenge(
                    challenge.code(), challenge.requestedAt(), challenge.attempts() + 1));
            throw new OtpException("The verification code is incorrect.");
        }
        if (!challenges.remove(phone, challenge)) {
            throw new OtpException("The verification code has already been used. Request a new one.");
        }
        Customer customer = customers.findByPhone(phone).orElseGet(() -> customers.save(new Customer(phone)));
        return tokens.issue(customer.getPhone(), "CUSTOMER");
    }

    private record Challenge(String code, Instant requestedAt, int attempts) {}
}
