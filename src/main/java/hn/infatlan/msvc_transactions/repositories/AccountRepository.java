package hn.infatlan.msvc_transactions.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hn.infatlan.msvc_transactions.entities.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Query("""
            SELECT a FROM Account a
            JOIN FETCH a.client
            JOIN FETCH a.status
            WHERE a.id = :id
            """)
    Optional<Account> findByIdWithDetails(@Param("id") UUID id);
}
