package hn.infatlan.msvc_transactions.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hn.infatlan.msvc_transactions.entities.IdempotencyRecord;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, UUID> {
    Optional<IdempotencyRecord> findByIdempotencyKeyAndAccountId(String idempotencyKey, UUID accountId);
}
