package hn.infatlan.msvc_transactions.services.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hn.infatlan.msvc_transactions.adapters.validation.ValidationPort;
import hn.infatlan.msvc_transactions.adapters.validation.ValidationResult;
import hn.infatlan.msvc_transactions.dtos.transaction.CreateTransactionRequestDTO;
import hn.infatlan.msvc_transactions.dtos.transaction.TransactionResponseDTO;
import hn.infatlan.msvc_transactions.entities.Account;
import hn.infatlan.msvc_transactions.entities.Client;
import hn.infatlan.msvc_transactions.entities.IdempotencyRecord;
import hn.infatlan.msvc_transactions.entities.Movement;
import hn.infatlan.msvc_transactions.entities.MovementStatus;
import hn.infatlan.msvc_transactions.entities.MovementType;
import hn.infatlan.msvc_transactions.enums.CatalogAccountStatus;
import hn.infatlan.msvc_transactions.enums.CatalogMovementStatus;
import hn.infatlan.msvc_transactions.enums.CatalogMovementType;
import hn.infatlan.msvc_transactions.enums.TransactionCode;
import hn.infatlan.msvc_transactions.enums.ValidationCode;
import hn.infatlan.msvc_transactions.exceptions.InfatlanTransactionException;
import hn.infatlan.msvc_transactions.repositories.AccountRepository;
import hn.infatlan.msvc_transactions.repositories.IdempotencyRecordRepository;
import hn.infatlan.msvc_transactions.repositories.MovementRepository;
import hn.infatlan.msvc_transactions.repositories.MovementStatusRepository;
import hn.infatlan.msvc_transactions.repositories.MovementTypeRepository;
import hn.infatlan.msvc_transactions.services.strategy.DebitTransactionStrategy;
import hn.infatlan.msvc_transactions.services.strategy.TransactionStrategyFactory;
import hn.infatlan.msvc_transactions.support.CatalogFixtures;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

        @Mock
        private AccountRepository accountRepository;
        @Mock
        private MovementRepository movementRepository;
        @Mock
        private MovementTypeRepository movementTypeRepository;
        @Mock
        private MovementStatusRepository movementStatusRepository;
        @Mock
        private IdempotencyRecordRepository idempotencyRecordRepository;
        @Mock
        private TransactionStrategyFactory strategyFactory;
        @Mock
        private ValidationPort validationPort;
        @Mock
        private ObjectMapper objectMapper;

        @InjectMocks
        private TransactionServiceImpl transactionService;

        private UUID accountId;
        private Account account;
        private CreateTransactionRequestDTO debitRequest;

        @BeforeEach
        void setUp() {
                accountId = UUID.randomUUID();
                account = Account.builder()
                                .id(accountId)
                                .accountNumber("123456789012")
                                .balance(new BigDecimal("200.0000"))
                                .currency("HNL")
                                .status(CatalogFixtures.accountStatus(CatalogAccountStatus.ACTIVE))
                                .client(Client.builder().id(UUID.randomUUID()).identityNumber("0801199012345")
                                                .fullName("Juan").build())
                                .build();
                debitRequest = CreateTransactionRequestDTO.builder()
                                .type("DEBIT")
                                .amount(new BigDecimal("50.0000"))
                                .description("Retiro")
                                .build();
        }

        @Test
        void createTransaction_confirmsDebitWhenValidationApproves() throws Exception {
                when(idempotencyRecordRepository
                                .findByIdempotencyKeyAndAccountId("123d4951-e6b9-4cf3-a675-f3c79533b812", accountId))
                                .thenReturn(Optional.empty());
                when(accountRepository.findByIdWithDetails(accountId)).thenReturn(Optional.of(account));
                when(strategyFactory.resolve("DEBIT")).thenReturn(new DebitTransactionStrategy());
                when(validationPort.authorize(any())).thenReturn(ValidationResult.builder()
                                .approved(true)
                                .authCode("AUTH-1")
                                .build());
                when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
                when(movementTypeRepository.findByCode(CatalogMovementType.DEBIT.name()))
                                .thenReturn(Optional.of(
                                                MovementType.builder().code("DEBIT").description("Debito").build()));
                when(movementStatusRepository.findByCode(CatalogMovementStatus.CONFIRMED.name()))
                                .thenReturn(Optional.of(MovementStatus.builder().code("CONFIRMED")
                                                .description("Confirmado").build()));
                when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> {
                        Movement movement = inv.getArgument(0);
                        movement.setId(UUID.randomUUID());
                        movement.setOccurredAt(LocalDateTime.now());
                        return movement;
                });
                when(objectMapper.writeValueAsString(any())).thenReturn("{}");

                TransactionResponseDTO response = transactionService.createTransaction(
                                accountId, debitRequest, "123d4951-e6b9-4cf3-a675-f3c79533b812", "tester",
                                "63a35c9c-d61e-4699-bbd9-64045bf1a21b");

                assertThat(response.getType()).isEqualTo("DEBIT");
                assertThat(response.getBalanceAfter()).isEqualByComparingTo("150.0000");
                assertThat(response.getExternalAuthRef()).isEqualTo("AUTH-1");
                verify(idempotencyRecordRepository).save(any(IdempotencyRecord.class));
        }

        @Test
        void createTransaction_rejectsWhenExternalValidationFails() {
                when(idempotencyRecordRepository
                                .findByIdempotencyKeyAndAccountId("123d4951-e6b9-4cf3-a675-f3c79533b812", accountId))
                                .thenReturn(Optional.empty());
                when(accountRepository.findByIdWithDetails(accountId)).thenReturn(Optional.of(account));
                when(strategyFactory.resolve("DEBIT")).thenReturn(new DebitTransactionStrategy());
                when(validationPort.authorize(any())).thenReturn(ValidationResult.builder()
                                .approved(false)
                                .reason("Rechazado por riesgo")
                                .build());

                assertThatThrownBy(() -> transactionService.createTransaction(
                                accountId, debitRequest, "123d4951-e6b9-4cf3-a675-f3c79533b812", "tester",
                                "63a35c9c-d61e-4699-bbd9-64045bf1a21b"))
                                .isInstanceOf(InfatlanTransactionException.class)
                                .extracting(ex -> ((InfatlanTransactionException) ex).getCodeCatalog())
                                .isEqualTo(ValidationCode.EXTERNAL_REJECTED);

                verify(movementRepository, never()).save(any());
        }

        @Test
        void createTransaction_returnsStoredResponseOnIdempotentReplay() throws Exception {
                TransactionResponseDTO stored = TransactionResponseDTO.builder()
                                .id(UUID.randomUUID())
                                .accountId(accountId)
                                .type("DEBIT")
                                .status("CONFIRMED")
                                .amount(new BigDecimal("50.0000"))
                                .balanceAfter(new BigDecimal("150.0000"))
                                .build();

                String hash = hashOf(debitRequest);
                when(idempotencyRecordRepository
                                .findByIdempotencyKeyAndAccountId("123d4951-e6b9-4cf3-a675-f3c79533b812", accountId))
                                .thenReturn(Optional.of(IdempotencyRecord.builder()
                                                .idempotencyKey("123d4951-e6b9-4cf3-a675-f3c79533b812")
                                                .accountId(accountId)
                                                .requestHash(hash)
                                                .responsePayload("{\"payload\":true}")
                                                .httpStatus(200)
                                                .build()));
                when(objectMapper.readValue("{\"payload\":true}", TransactionResponseDTO.class)).thenReturn(stored);

                TransactionResponseDTO response = transactionService.createTransaction(
                                accountId, debitRequest, "123d4951-e6b9-4cf3-a675-f3c79533b812", "tester",
                                "63a35c9c-d61e-4699-bbd9-64045bf1a21b");

                assertThat(response).isSameAs(stored);
                verify(validationPort, never()).authorize(any());
        }

        @Test
        void createTransaction_conflictsWhenIdempotencyKeyReusedWithDifferentBody() {
                when(idempotencyRecordRepository
                                .findByIdempotencyKeyAndAccountId("123d4951-e6b9-4cf3-a675-f3c79533b812", accountId))
                                .thenReturn(Optional.of(IdempotencyRecord.builder()
                                                .idempotencyKey("123d4951-e6b9-4cf3-a675-f3c79533b812")
                                                .accountId(accountId)
                                                .requestHash("different-hash")
                                                .responsePayload("{}")
                                                .httpStatus(200)
                                                .build()));

                assertThatThrownBy(() -> transactionService.createTransaction(
                                accountId, debitRequest, "123d4951-e6b9-4cf3-a675-f3c79533b812", "tester",
                                "63a35c9c-d61e-4699-bbd9-64045bf1a21b"))
                                .isInstanceOf(InfatlanTransactionException.class)
                                .extracting(ex -> ((InfatlanTransactionException) ex).getCodeCatalog())
                                .isEqualTo(TransactionCode.IDEMPOTENCY_CONFLICT);
        }

        private static String hashOf(CreateTransactionRequestDTO request) throws Exception {
                String raw = request.getType() + "|" + request.getAmount().toPlainString() + "|"
                                + (request.getDescription() == null ? "" : request.getDescription());
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hashed = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                return java.util.HexFormat.of().formatHex(hashed);
        }
}
