package hn.infatlan.msvc_transactions.services.implementations;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import hn.infatlan.msvc_transactions.adapters.validation.ValidationPort;
import hn.infatlan.msvc_transactions.adapters.validation.ValidationRequest;
import hn.infatlan.msvc_transactions.adapters.validation.ValidationResult;
import hn.infatlan.msvc_transactions.config.JpaAuditingConfig;
import hn.infatlan.msvc_transactions.dtos.transaction.CreateTransactionRequestDTO;
import hn.infatlan.msvc_transactions.dtos.transaction.TransactionResponseDTO;
import hn.infatlan.msvc_transactions.entities.Account;
import hn.infatlan.msvc_transactions.entities.IdempotencyRecord;
import hn.infatlan.msvc_transactions.entities.Movement;
import hn.infatlan.msvc_transactions.entities.MovementStatus;
import hn.infatlan.msvc_transactions.entities.MovementType;
import hn.infatlan.msvc_transactions.enums.AccountCode;
import hn.infatlan.msvc_transactions.enums.CatalogMovementStatus;
import hn.infatlan.msvc_transactions.enums.CatalogMovementType;
import hn.infatlan.msvc_transactions.enums.ProcessLogCatalog;
import hn.infatlan.msvc_transactions.enums.TransactionCode;
import hn.infatlan.msvc_transactions.enums.TypeLogCatalog;
import hn.infatlan.msvc_transactions.enums.ValidationCode;
import hn.infatlan.msvc_transactions.repositories.AccountRepository;
import hn.infatlan.msvc_transactions.repositories.IdempotencyRecordRepository;
import hn.infatlan.msvc_transactions.repositories.MovementRepository;
import hn.infatlan.msvc_transactions.repositories.MovementStatusRepository;
import hn.infatlan.msvc_transactions.repositories.MovementTypeRepository;
import hn.infatlan.msvc_transactions.services.definitions.TransactionService;
import hn.infatlan.msvc_transactions.services.strategy.TransactionStrategy;
import hn.infatlan.msvc_transactions.services.strategy.TransactionStrategyFactory;
import hn.infatlan.msvc_transactions.util.ExceptionFactory;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final MovementTypeRepository movementTypeRepository;
    private final MovementStatusRepository movementStatusRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final TransactionStrategyFactory strategyFactory;
    private final ValidationPort validationPort;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(
            UUID accountId,
            CreateTransactionRequestDTO request,
            String idempotencyKey,
            String identifierUser,
            String correlationId) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw ExceptionFactory.business(
                    TransactionCode.IDEMPOTENCY_KEY_REQUIRED,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TRANSACTION,
                    null);
        }

        setAuditorForRequest(identifierUser);

        String requestHash = hashRequest(request);
        var existing = idempotencyRecordRepository.findByIdempotencyKeyAndAccountId(idempotencyKey, accountId);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (!record.getRequestHash().equals(requestHash)) {
                throw ExceptionFactory.business(
                        TransactionCode.IDEMPOTENCY_CONFLICT,
                        ProcessLogCatalog.CREATE_TRANSACTION,
                        TypeLogCatalog.TRANSACTION,
                        null);
            }
            return deserializeStoredResponse(record.getResponsePayload());
        }

        Account account = accountRepository.findByIdWithDetails(accountId)
                .orElseThrow(() -> ExceptionFactory.business(
                        AccountCode.ACCOUNT_NOT_FOUND,
                        ProcessLogCatalog.CREATE_TRANSACTION,
                        TypeLogCatalog.TRANSACTION,
                        null));

        TransactionStrategy strategy = strategyFactory.resolve(request.getType());
        strategy.validate(account, request.getAmount());

        ValidationResult validationResult = validationPort.authorize(ValidationRequest.builder()
                .accountId(accountId)
                .type(request.getType())
                .amount(request.getAmount())
                .currency(account.getCurrency())
                .correlationId(correlationId)
                .build());

        if (!validationResult.approved()) {
            throw ExceptionFactory.business(
                    ValidationCode.EXTERNAL_REJECTED,
                    ProcessLogCatalog.EXTERNAL_VALIDATION,
                    TypeLogCatalog.VALIDATION,
                    validationResult.reason());
        }

        try {
            BigDecimal balanceAfter = strategy.apply(account, request.getAmount());
            accountRepository.save(account);

            MovementType movementType = movementTypeRepository
                    .findByCode(CatalogMovementType.valueOf(request.getType()).name())
                    .orElseThrow(() -> ExceptionFactory.business(
                            TransactionCode.INVALID_MOVEMENT_TYPE,
                            ProcessLogCatalog.CREATE_TRANSACTION,
                            TypeLogCatalog.TRANSACTION,
                            null));

            MovementStatus confirmedStatus = movementStatusRepository
                    .findByCode(CatalogMovementStatus.CONFIRMED.name())
                    .orElseThrow(() -> ExceptionFactory.business(
                            TransactionCode.TRANSACTION_CONFIRMED,
                            ProcessLogCatalog.CREATE_TRANSACTION,
                            TypeLogCatalog.TRANSACTION,
                            "No se encontró el estado CONFIRMED del catálogo de movimientos."));

            Movement movement = Movement.builder()
                    .account(account)
                    .type(movementType)
                    .status(confirmedStatus)
                    .amount(request.getAmount())
                    .balanceAfter(balanceAfter)
                    .description(request.getDescription())
                    .correlationId(correlationId)
                    .externalAuthRef(validationResult.authCode())
                    .build();

            Movement saved = movementRepository.save(movement);
            TransactionResponseDTO response = toResponse(saved);

            persistIdempotency(idempotencyKey, accountId, requestHash, response);
            return response;
        } catch (OptimisticLockingFailureException ex) {
            throw ExceptionFactory.business(
                    TransactionCode.CONCURRENT_MODIFICATION,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TRANSACTION,
                    null);
        }
    }

    private void persistIdempotency(
            String idempotencyKey,
            UUID accountId,
            String requestHash,
            TransactionResponseDTO response) {
        try {
            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .accountId(accountId)
                    .requestHash(requestHash)
                    .responsePayload(objectMapper.writeValueAsString(response))
                    .httpStatus(HttpStatus.OK.value())
                    .build();
            idempotencyRecordRepository.save(record);
        } catch (Exception ex) {
            throw ExceptionFactory.business(
                    TransactionCode.TRANSACTION_CONFIRMED,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TECHNICAL,
                    "No fue posible persistir el registro de idempotencia.");
        }
    }

    private TransactionResponseDTO deserializeStoredResponse(String payload) {
        try {
            return objectMapper.readValue(payload, TransactionResponseDTO.class);
        } catch (Exception ex) {
            throw ExceptionFactory.business(
                    TransactionCode.IDEMPOTENCY_CONFLICT,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TECHNICAL,
                    "No fue posible recuperar la respuesta idempotente almacenada.");
        }
    }

    private String hashRequest(CreateTransactionRequestDTO request) {
        String raw = request.getType() + "|" + request.getAmount().toPlainString() + "|"
                + (request.getDescription() == null ? "" : request.getDescription());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private void setAuditorForRequest(String identifierUser) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(
                    JpaAuditingConfig.AUDITOR_ATTRIBUTE,
                    identifierUser,
                    RequestAttributes.SCOPE_REQUEST);
        }
    }

    private TransactionResponseDTO toResponse(Movement movement) {
        return TransactionResponseDTO.builder()
                .id(movement.getId())
                .accountId(movement.getAccount().getId())
                .type(movement.getType().getCode())
                .status(movement.getStatus().getCode())
                .amount(movement.getAmount())
                .balanceAfter(movement.getBalanceAfter())
                .description(movement.getDescription())
                .occurredAt(movement.getOccurredAt())
                .externalAuthRef(movement.getExternalAuthRef())
                .build();
    }
}
