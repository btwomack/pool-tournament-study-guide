package com.pooltournament.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    public Map<String, String> createPaymentIntent(Long amount, String currency, UUID tournamentId, UUID userId) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .putMetadata("tournamentId", tournamentId.toString())
                .putMetadata("userId", userId.toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        
        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("paymentIntentId", intent.getId());
        return response;
    }
}
