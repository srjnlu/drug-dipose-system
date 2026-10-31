package com.goodcycle.identity.otp;

public interface SmsSender {
    void send(String phone, String message);
}
