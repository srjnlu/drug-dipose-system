package com.goodcycle.identity.otp;

import com.goodcycle.security.JwtTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequestMapping("/api/auth/local-token")
public class DevAuthController {
    private final JwtTokenService tokens;

    public DevAuthController(JwtTokenService tokens) {
        this.tokens = tokens;
    }

    @PostMapping
    public AuthController.AuthResponse issue(@Valid @RequestBody DevTokenRequest request) {
        return new AuthController.AuthResponse(tokens.issue(request.phone(), request.role()), "Bearer");
    }

    public record DevTokenRequest(
            @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$") String phone,
            @NotBlank @Pattern(regexp = "^(CUSTOMER|SUPPORT|ADMIN)$") String role) {}
}
