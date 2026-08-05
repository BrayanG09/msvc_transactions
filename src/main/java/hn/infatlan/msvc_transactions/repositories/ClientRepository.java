package hn.infatlan.msvc_transactions.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hn.infatlan.msvc_transactions.entities.Client;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query("""
            SELECT c FROM Client c
            JOIN FETCH c.status
            WHERE c.identityNumber = :identityNumber
            """)
    Optional<Client> findByIdentityNumber(@Param("identityNumber") String identityNumber);

    boolean existsByIdentityNumber(String identityNumber);

    @Query("""
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM Client c
                WHERE c.email IS NOT NULL AND LOWER(c.email) = LOWER(:email)
            ) THEN true ELSE false END
            """)
    boolean existsByEmailIgnoreCase(@Param("email") String email);
}
