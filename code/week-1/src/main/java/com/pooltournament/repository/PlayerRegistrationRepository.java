package com.pooltournament.repository;

import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.entity.Tournament;
import com.pooltournament.enums.PlayerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRegistrationRepository extends JpaRepository<PlayerRegistration, UUID> {
    List<PlayerRegistration> findByTournamentAndStatus(Tournament tournament, PlayerStatus status);
    Optional<PlayerRegistration> findByPaymentIntentId(String paymentIntentId);

    @Modifying
    @Query("UPDATE PlayerRegistration p SET p.status = 'CONFIRMED', p.paidFlag = true WHERE p.paymentIntentId = :paymentIntentId")
    void confirmByPaymentIntentId(String paymentIntentId);
}
