package hn.infatlan.msvc_transactions.services.implementations;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hn.infatlan.msvc_transactions.dtos.statement.StatementResponseDTO;
import hn.infatlan.msvc_transactions.enums.ProcessLogCatalog;
import hn.infatlan.msvc_transactions.enums.StatementCode;
import hn.infatlan.msvc_transactions.enums.TypeLogCatalog;
import hn.infatlan.msvc_transactions.repositories.StatementProcedureRepository;
import hn.infatlan.msvc_transactions.services.definitions.StatementService;
import hn.infatlan.msvc_transactions.util.ExceptionFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;

    private final StatementProcedureRepository statementProcedureRepository;

    @Override
    @Transactional(readOnly = true)
    public StatementResponseDTO getStatement(
            UUID accountId,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {
        validateBeforeCall(from, to, page, size);
        return statementProcedureRepository.executeEstadoCuenta(accountId, from, to, page, size);
    }

    private void validateBeforeCall(LocalDate from, LocalDate to, int page, int size) {
        if (from == null || to == null || from.isAfter(to)) {
            throw ExceptionFactory.business(
                    StatementCode.INVALID_DATE_RANGE,
                    ProcessLogCatalog.GET_STATEMENT,
                    TypeLogCatalog.STATEMENT,
                    null);
        }
        if (page < 0 || size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw ExceptionFactory.business(
                    StatementCode.INVALID_PAGE_SIZE,
                    ProcessLogCatalog.GET_STATEMENT,
                    TypeLogCatalog.STATEMENT,
                    null);
        }
    }
}
