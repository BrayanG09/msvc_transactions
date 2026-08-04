package hn.infatlan.msvc_transactions.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hn.infatlan.msvc_transactions.entities.Client;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByIdentityNumber(String identityNumber);

    boolean existsByIdentityNumber(String identityNumber);
}
