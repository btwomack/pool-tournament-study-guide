package com.pooltournament.repository;

import com.pooltournament.entity.CalcuttaBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CalcuttaBidRepository extends JpaRepository<CalcuttaBid, UUID> {
}
