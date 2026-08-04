package hn.infatlan.msvc_transactions.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hn.infatlan.msvc_transactions.entities.MovementType;

public interface MovementTypeRepository extends JpaRepository<MovementType, UUID> {
    Optional<MovementType> findByCode(String code);
}
