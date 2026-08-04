package hn.infatlan.msvc_transactions.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hn.infatlan.msvc_transactions.entities.ClientStatus;

public interface ClientStatusRepository extends JpaRepository<ClientStatus, UUID> {
    Optional<ClientStatus> findByCode(String code);
}
