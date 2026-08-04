package hn.infatlan.msvc_transactions.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hn.infatlan.msvc_transactions.entities.Movement;

public interface MovementRepository extends JpaRepository<Movement, UUID> {
}
