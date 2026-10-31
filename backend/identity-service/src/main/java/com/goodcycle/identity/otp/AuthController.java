package com.goodcycle.identity.otp;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/otp")
public class AuthController {
    private final OtpService otpService;

    public AuthController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/request")
    public OtpRequestResponse request(@Valid @RequestBody OtpRequest request) {
        String code = otpService.requestCode(request.phone());
        String message = code == null
                ? "If the number is eligible, a verification code has been sent."
                : "Local demo code generated. No SMS was sent.";
        return new OtpRequestResponse(message, code);
    }

    @PostMapping("/verify")
    public AuthResponse verify(@Valid @RequestBody OtpVerify request) {
        return new AuthResponse(otpService.verify(request.phone(), request.code()), "Bearer");
    }

    @ExceptionHandler(OtpException.class)
    ResponseEntity<ApiError> handleOtpError(OtpException error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(error.getMessage()));
    }

    public record OtpRequest(@NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$") String phone) {}
    public record OtpVerify(
            @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$") String phone,
            @NotBlank @Size(min = 6, max = 6) @Pattern(regexp = "^\\d{6}$") String code) {}
    public record OtpRequestResponse(String message, String demoCode) {}
    public record AuthResponse(String accessToken, String tokenType) {}
    public record ApiError(String message) {}
}
