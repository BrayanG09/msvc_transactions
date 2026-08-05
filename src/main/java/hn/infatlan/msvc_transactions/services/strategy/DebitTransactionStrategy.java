package hn.infatlan.msvc_transactions.services.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import hn.infatlan.msvc_transactions.entities.Account;
import hn.infatlan.msvc_transactions.enums.AccountCode;
import hn.infatlan.msvc_transactions.enums.CatalogAccountStatus;
import hn.infatlan.msvc_transactions.enums.CatalogMovementType;
import hn.infatlan.msvc_transactions.enums.ProcessLogCatalog;
import hn.infatlan.msvc_transactions.enums.TransactionCode;
import hn.infatlan.msvc_transactions.enums.TypeLogCatalog;
import hn.infatlan.msvc_transactions.util.ExceptionFactory;

@Component
public class DebitTransactionStrategy implements TransactionStrategy {

    @Override
    public CatalogMovementType supports() {
        return CatalogMovementType.DEBIT;
    }

    @Override
    public void validate(Account account, BigDecimal amount) {
        ensureActive(account);
        if (account.getBalance().compareTo(amount) < 0) {
            throw ExceptionFactory.business(
                    TransactionCode.INSUFFICIENT_FUNDS,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TRANSACTION,
                    null);
        }
    }

    @Override
    public BigDecimal apply(Account account, BigDecimal amount) {
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        return newBalance;
    }

    private void ensureActive(Account account) {
        String status = account.getStatus().getCode();
        if (CatalogAccountStatus.BLOCKED.name().equals(status)) {
            throw ExceptionFactory.business(
                    AccountCode.ACCOUNT_BLOCKED,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TRANSACTION,
                    null);
        }
        if (CatalogAccountStatus.CLOSED.name().equals(status)) {
            throw ExceptionFactory.business(
                    AccountCode.ACCOUNT_CLOSED,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TRANSACTION,
                    null);
        }
        if (!CatalogAccountStatus.ACTIVE.name().equals(status)) {
            throw ExceptionFactory.business(
                    AccountCode.ACCOUNT_BLOCKED,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TRANSACTION,
                    null);
        }
    }
}
