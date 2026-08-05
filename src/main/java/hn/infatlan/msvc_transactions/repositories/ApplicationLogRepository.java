package hn.infatlan.msvc_transactions.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hn.infatlan.msvc_transactions.entities.ApplicationLog;

public interface ApplicationLogRepository extends JpaRepository<ApplicationLog, UUID> {
}
