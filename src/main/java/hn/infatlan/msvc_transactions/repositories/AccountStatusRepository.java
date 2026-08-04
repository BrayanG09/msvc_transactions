package hn.infatlan.msvc_transactions.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hn.infatlan.msvc_transactions.entities.AccountStatus;

public interface AccountStatusRepository extends JpaRepository<AccountStatus, UUID> {
    Optional<AccountStatus> findByCode(String code);
}
