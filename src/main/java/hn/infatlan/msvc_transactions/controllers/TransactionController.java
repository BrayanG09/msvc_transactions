package hn.infatlan.msvc_transactions.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hn.infatlan.msvc_transactions.dtos.common.ResponseDTO;
import hn.infatlan.msvc_transactions.dtos.transaction.CreateTransactionRequestDTO;
import hn.infatlan.msvc_transactions.dtos.transaction.TransactionResponseDTO;
import hn.infatlan.msvc_transactions.enums.TransactionCode;
import hn.infatlan.msvc_transactions.services.definitions.TransactionService;
import hn.infatlan.msvc_transactions.util.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts/{accountId}/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transactions", description = "Registro de movimientos CREDIT/DEBIT")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Registrar un movimiento", description = "Registra un CREDIT o DEBIT con idempotencia y validación externa.")
    public ResponseEntity<ResponseDTO<TransactionResponseDTO>> createTransaction(
            @PathVariable("accountId") UUID accountId,
            @Valid @RequestBody CreateTransactionRequestDTO request,
            @RequestHeader(RequestHeaders.IDENTIFIER_USER)
            @NotBlank(message = "El encabezado identifier-user es obligatorio.")
            String identifierUser,
            @RequestHeader(RequestHeaders.IDEMPOTENCY_KEY)
            @NotBlank(message = "El encabezado Idempotency-Key es obligatorio.")
            String idempotencyKey,
            @RequestHeader(value = RequestHeaders.CORRELATION_ID, required = false)
            String correlationId) {

        String effectiveCorrelationId = (correlationId == null || correlationId.isBlank())
                ? UUID.randomUUID().toString()
                : correlationId;

        TransactionResponseDTO data = transactionService.createTransaction(
                accountId,
                request,
                idempotencyKey,
                identifierUser,
                effectiveCorrelationId);

        return ResponseEntity.ok(ResponseDTO.of(TransactionCode.TRANSACTION_CONFIRMED, data));
    }
}
