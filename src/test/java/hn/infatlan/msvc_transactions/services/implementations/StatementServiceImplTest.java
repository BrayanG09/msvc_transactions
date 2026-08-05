package hn.infatlan.msvc_transactions.services.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hn.infatlan.msvc_transactions.dtos.statement.StatementResponseDTO;
import hn.infatlan.msvc_transactions.enums.StatementCode;
import hn.infatlan.msvc_transactions.exceptions.InfatlanTransactionException;
import hn.infatlan.msvc_transactions.repositories.StatementProcedureRepository;

@ExtendWith(MockitoExtension.class)
class StatementServiceImplTest {

    @Mock
    private StatementProcedureRepository statementProcedureRepository;

    @InjectMocks
    private StatementServiceImpl statementService;

    @Test
    void getStatement_delegatesToProcedureRepository() {
        UUID accountId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        StatementResponseDTO expected = StatementResponseDTO.builder()
                .accountId(accountId)
                .from(from)
                .to(to)
                .movements(Collections.emptyList())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .build();

        when(statementProcedureRepository.executeEstadoCuenta(accountId, from, to, 0, 20))
                .thenReturn(expected);

        StatementResponseDTO result = statementService.getStatement(accountId, from, to, 0, 20);

        assertThat(result).isSameAs(expected);
        verify(statementProcedureRepository).executeEstadoCuenta(accountId, from, to, 0, 20);
    }

    @Test
    void getStatement_rejectsInvalidDateRange() {
        UUID accountId = UUID.randomUUID();

        assertThatThrownBy(() -> statementService.getStatement(
                accountId,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1),
                0,
                20))
                .isInstanceOf(InfatlanTransactionException.class)
                .extracting(ex -> ((InfatlanTransactionException) ex).getCodeCatalog())
                .isEqualTo(StatementCode.INVALID_DATE_RANGE);

        verify(statementProcedureRepository, never()).executeEstadoCuenta(any(), any(), any(), eq(0), eq(20));
    }
}
