package hn.infatlan.msvc_transactions.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import hn.infatlan.msvc_transactions.dtos.statement.StatementResponseDTO;
import hn.infatlan.msvc_transactions.enums.StatementCode;
import hn.infatlan.msvc_transactions.filters.CorrelationIdFilter;
import hn.infatlan.msvc_transactions.handlers.CorrelationResponseAdvice;
import hn.infatlan.msvc_transactions.handlers.GlobalExceptionHandler;
import hn.infatlan.msvc_transactions.services.definitions.ApplicationLogService;
import hn.infatlan.msvc_transactions.services.definitions.StatementService;
import hn.infatlan.msvc_transactions.util.RequestHeaders;

@WebMvcTest(controllers = StatementController.class)
@Import({GlobalExceptionHandler.class, CorrelationResponseAdvice.class, CorrelationIdFilter.class})
class StatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatementService statementService;

    @MockitoBean
    private ApplicationLogService applicationLogService;

    @Test
    void getStatement_returnsOk() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(statementService.getStatement(eq(accountId), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt()))
                .thenReturn(StatementResponseDTO.builder()
                        .accountId(accountId)
                        .accountNumber("123456789012")
                        .from(LocalDate.of(2026, 1, 1))
                        .to(LocalDate.of(2026, 1, 31))
                        .openingBalance(BigDecimal.ZERO)
                        .closingBalance(BigDecimal.TEN)
                        .movements(Collections.emptyList())
                        .page(0)
                        .size(20)
                        .totalElements(0)
                        .totalPages(0)
                        .build());

        mockMvc.perform(post("/accounts/{accountId}/statement", accountId)
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31")
                        .param("page", "0")
                        .param("size", "20")
                        .header(RequestHeaders.IDENTIFIER_USER, "tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatementCode.STATEMENT_GENERATED.code()))
                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                .andExpect(jsonPath("$.data.accountId").value(accountId.toString()));
    }
}
