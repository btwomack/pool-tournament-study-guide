package com.pooltournament.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Value("${twilio.account.sid:AC_placeholder}")
    private String accountSid;

    @Value("${twilio.auth.token:AT_placeholder}")
    private String authToken;

    @Value("${twilio.phone.number:+1234567890}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void notifyMatchReady(String toPhoneNumber, String playerName, String opponentName, String matchLabel) {
        String body = String.format("Hi %s! Your match %s against %s is ready to play. Please report to the tournament desk.", 
                playerName, matchLabel, opponentName);
        
        try {
            Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                body
            ).create();
        } catch (Exception e) {
            // Log error, but don't fail the transaction
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }
}
