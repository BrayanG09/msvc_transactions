package hn.infatlan.msvc_transactions.services.definitions;

import java.time.LocalDate;
import java.util.UUID;

import hn.infatlan.msvc_transactions.dtos.statement.StatementResponseDTO;

public interface StatementService {

    StatementResponseDTO getStatement(
            UUID accountId,
            LocalDate from,
            LocalDate to,
            int page,
            int size);
}
