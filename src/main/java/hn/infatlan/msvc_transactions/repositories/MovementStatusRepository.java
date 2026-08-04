package hn.infatlan.msvc_transactions.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hn.infatlan.msvc_transactions.entities.MovementStatus;

public interface MovementStatusRepository extends JpaRepository<MovementStatus, UUID> {
    Optional<MovementStatus> findByCode(String code);
}
