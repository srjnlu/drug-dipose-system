package com.goodcycle.identity.otp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@Profile("!local")
public class TwilioSmsSender implements SmsSender {
    private final RestClient restClient;
    private final String accountSid;
    private final String fromNumber;

    public TwilioSmsSender(
            RestClient.Builder builder,
            @Value("${TWILIO_ACCOUNT_SID}") String accountSid,
            @Value("${TWILIO_AUTH_TOKEN}") String authToken,
            @Value("${TWILIO_FROM_NUMBER}") String fromNumber) {
        this.accountSid = accountSid;
        this.fromNumber = fromNumber;
        String basic = Base64.getEncoder().encodeToString(
                (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        this.restClient = builder
                .baseUrl("https://api.twilio.com/2010-04-01/Accounts/" + accountSid)
                .defaultHeader("Authorization", "Basic " + basic)
                .build();
    }

    @Override
    public void send(String phone, String message) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", phone);
        form.add("From", fromNumber);
        form.add("Body", message);
        restClient.post()
                .uri("/Messages.json")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }
}
