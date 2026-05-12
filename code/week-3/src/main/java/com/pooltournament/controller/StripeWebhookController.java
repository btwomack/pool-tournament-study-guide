package com.pooltournament.controller;

import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.User;
import com.pooltournament.enums.PlayerStatus;
import com.pooltournament.repository.PlayerRegistrationRepository;
import com.pooltournament.repository.TournamentRepository;
import com.pooltournament.repository.UserRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook.secret:whsec_placeholder}")
    private String endpointSecret;

    private final PlayerRegistrationRepository playerRepo;
    private final TournamentRepository tournamentRepo;
    private final UserRepository userRepo;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook Error: " + e.getMessage());
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null) {
                handlePaymentSuccess(paymentIntent);
            }
        }

        return ResponseEntity.ok("Success");
    }

    private void handlePaymentSuccess(PaymentIntent intent) {
        String tIdStr = intent.getMetadata().get("tournamentId");
        String uIdStr = intent.getMetadata().get("userId");
        String playerName = intent.getMetadata().get("playerName");
        String phoneNumber = intent.getMetadata().get("phoneNumber");

        if (tIdStr != null && uIdStr != null) {
            UUID tournamentId = UUID.fromString(tIdStr);
            UUID userId = UUID.fromString(uIdStr);

            Tournament t = tournamentRepo.findById(tournamentId).orElse(null);
            User u = userRepo.findById(userId).orElse(null);

            if (t != null && u != null) {
                PlayerRegistration reg = PlayerRegistration.builder()
                        .tournament(t)
                        .user(u)
                        .playerName(playerName != null ? playerName : u.getEmail())
                        .phoneNumber(phoneNumber)
                        .status(PlayerStatus.CONFIRMED)
                        .entryFeePaid(true)
                        .stripePaymentId(intent.getId())
                        .build();
                playerRepo.save(reg);
            }
        }
    }
}
