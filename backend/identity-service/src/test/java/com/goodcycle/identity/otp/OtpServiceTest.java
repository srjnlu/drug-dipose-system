package com.goodcycle.identity.otp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.goodcycle.identity.user.Customer;
import com.goodcycle.identity.user.CustomerRepository;
import com.goodcycle.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class OtpServiceTest {
    private static final String PHONE = "+919876543210";
    private final CustomerRepository customers = org.mockito.Mockito.mock(CustomerRepository.class);
    private final SmsSender smsSender = org.mockito.Mockito.mock(SmsSender.class);
    private final JwtTokenService tokens = org.mockito.Mockito.mock(JwtTokenService.class);
    private final OtpService service = new OtpService(
            customers, smsSender, tokens, false, new MockEnvironment());

    @BeforeEach
    void setUp() {
        when(customers.findByPhone(PHONE)).thenReturn(Optional.empty());
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokens.issue(PHONE, "CUSTOMER")).thenReturn("access-token");
    }

    @Test
    void verifiesAnOtpAndConsumesItAfterOneUse() {
        service.requestCode(PHONE);
        org.mockito.ArgumentCaptor<String> message = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(smsSender).send(org.mockito.ArgumentMatchers.eq(PHONE), message.capture());
        Matcher matcher = Pattern.compile("verification code is (\\d{6})").matcher(message.getValue());
        assertTrue(matcher.find());

        assertEquals("access-token", service.verify(PHONE, matcher.group(1)));
        assertThrows(OtpException.class, () -> service.verify(PHONE, matcher.group(1)));
        verify(customers).save(any(Customer.class));
    }

    @Test
    void enforcesTheOtpResendCooldown() {
        service.requestCode(PHONE);

        assertThrows(OtpException.class, () -> service.requestCode(PHONE));
        verify(smsSender).send(org.mockito.ArgumentMatchers.eq(PHONE), any(String.class));
    }
}
