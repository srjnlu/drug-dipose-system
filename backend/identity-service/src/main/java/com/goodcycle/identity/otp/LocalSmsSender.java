package com.goodcycle.identity.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalSmsSender implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(LocalSmsSender.class);

    @Override
    public void send(String phone, String message) {
        log.info("Local OTP issued for phone ending {}", phone.substring(phone.length() - 4));
    }
}
