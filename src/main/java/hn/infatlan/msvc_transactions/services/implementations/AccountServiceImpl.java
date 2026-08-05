package hn.infatlan.msvc_transactions.services.implementations;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import hn.infatlan.msvc_transactions.config.JpaAuditingConfig;
import hn.infatlan.msvc_transactions.dtos.account.AccountResponseDTO;
import hn.infatlan.msvc_transactions.dtos.account.CreateAccountRequestDTO;
import hn.infatlan.msvc_transactions.entities.Account;
import hn.infatlan.msvc_transactions.entities.AccountStatus;
import hn.infatlan.msvc_transactions.entities.Client;
import hn.infatlan.msvc_transactions.entities.Movement;
import hn.infatlan.msvc_transactions.entities.MovementStatus;
import hn.infatlan.msvc_transactions.entities.MovementType;
import hn.infatlan.msvc_transactions.enums.AccountCode;
import hn.infatlan.msvc_transactions.enums.CatalogAccountStatus;
import hn.infatlan.msvc_transactions.enums.CatalogMovementStatus;
import hn.infatlan.msvc_transactions.enums.CatalogMovementType;
import hn.infatlan.msvc_transactions.enums.ProcessLogCatalog;
import hn.infatlan.msvc_transactions.repositories.AccountRepository;
import hn.infatlan.msvc_transactions.repositories.AccountStatusRepository;
import hn.infatlan.msvc_transactions.repositories.MovementRepository;
import hn.infatlan.msvc_transactions.repositories.MovementStatusRepository;
import hn.infatlan.msvc_transactions.repositories.MovementTypeRepository;
import hn.infatlan.msvc_transactions.services.definitions.AccountService;
import hn.infatlan.msvc_transactions.services.definitions.ClientService;
import hn.infatlan.msvc_transactions.util.CustomUtils;
import hn.infatlan.msvc_transactions.util.ExceptionFactory;
import hn.infatlan.msvc_transactions.util.MaskingUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final String DEFAULT_CURRENCY = "HNL";
    private static final int MAX_ACCOUNT_NUMBER_ATTEMPTS = 10;

    private final AccountRepository accountRepository;
    private final AccountStatusRepository accountStatusRepository;
    private final MovementRepository movementRepository;
    private final MovementTypeRepository movementTypeRepository;
    private final MovementStatusRepository movementStatusRepository;
    private final ClientService clientService;

    @Override
    @Transactional
    public AccountResponseDTO createAccount(CreateAccountRequestDTO request) {
        BigDecimal initialBalance = request.getInitialBalance();
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw ExceptionFactory.business(AccountCode.INVALID_INITIAL_BALANCE, ProcessLogCatalog.CREATE_ACCOUNT);
        }

        setAuditorForRequest(request.getIdentityNumber());

        Client client = clientService.findOrCreate(
                request.getIdentityNumber(),
                request.getFullName(),
                request.getEmail());

        if (client.getStatus() == null || !CatalogAccountStatus.ACTIVE.name().equals(client.getStatus().getCode())) {
            throw ExceptionFactory.business(
                    AccountCode.CLIENT_INACTIVE,
                    ProcessLogCatalog.CREATE_ACCOUNT,
                    "El cliente con número de identidad " + request.getIdentityNumber() + " no está activo.");
        }

        AccountStatus activeStatus = this.accountStatusRepository.findByCode(CatalogAccountStatus.ACTIVE.name())
                .orElseThrow(() -> ExceptionFactory.business(
                        AccountCode.ACCOUNT_NOT_FOUND,
                        ProcessLogCatalog.CREATE_ACCOUNT,
                        "No se encontró el estado " + CatalogAccountStatus.ACTIVE.name() + " del catálogo de cuentas."));

        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .client(client)
                .balance(initialBalance)
                .currency(DEFAULT_CURRENCY)
                .status(activeStatus)
                .build();

        Account savedAccount = this.accountRepository.save(account);

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            this.createOpeningMovement(savedAccount, initialBalance);
        }

        return this.toResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountById(UUID accountId) {
        Account account = this.accountRepository.findByIdWithDetails(accountId)
                .orElseThrow(() -> ExceptionFactory.business(
                        AccountCode.ACCOUNT_NOT_FOUND,
                        ProcessLogCatalog.GET_ACCOUNT));

        return this.toResponse(account);
    }

    private String generateUniqueAccountNumber() {
        for (int attempt = 1; attempt <= MAX_ACCOUNT_NUMBER_ATTEMPTS; attempt++) {
            String candidate = CustomUtils.generateAccountNumber();
            if (!accountRepository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }

        throw ExceptionFactory.business(
                AccountCode.ACCOUNT_NUMBER_GENERATION_FAILED,
                ProcessLogCatalog.CREATE_ACCOUNT);
    }

    private void createOpeningMovement(Account account, BigDecimal amount) {
        MovementType openingType = this.movementTypeRepository.findByCode(CatalogMovementType.OPENING.name())
                .orElseThrow(() -> ExceptionFactory.business(
                        AccountCode.ACCOUNT_CREATED,
                        ProcessLogCatalog.CREATE_ACCOUNT,
                        "No se encontró el tipo de movimiento OPENING."));

        MovementStatus confirmedStatus = this.movementStatusRepository.findByCode(CatalogMovementStatus.CONFIRMED.name())
                .orElseThrow(() -> ExceptionFactory.business(
                        AccountCode.ACCOUNT_CREATED,
                        ProcessLogCatalog.CREATE_ACCOUNT,
                        "No se encontró el estado CONFIRMED del catálogo de movimientos."));

        Movement opening = Movement.builder()
                .account(account)
                .type(openingType)
                .status(confirmedStatus)
                .amount(amount)
                .balanceAfter(amount)
                .description("Saldo inicial de apertura de cuenta")
                .build();

        this.movementRepository.save(opening);
    }

    private void setAuditorForRequest(String identityNumber) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(
                    JpaAuditingConfig.AUDITOR_ATTRIBUTE,
                    identityNumber,
                    RequestAttributes.SCOPE_REQUEST);
        }
    }

    private AccountResponseDTO toResponse(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .accountNumber(MaskingUtils.maskAccountNumber(account.getAccountNumber()))
                .clientId(account.getClient().getId())
                .identityNumber(account.getClient().getIdentityNumber())
                .fullName(account.getClient().getFullName())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus().getCode())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
