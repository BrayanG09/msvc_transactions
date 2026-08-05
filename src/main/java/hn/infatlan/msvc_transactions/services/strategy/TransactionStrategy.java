package hn.infatlan.msvc_transactions.services.strategy;

import java.math.BigDecimal;

import hn.infatlan.msvc_transactions.entities.Account;
import hn.infatlan.msvc_transactions.enums.CatalogMovementType;

public interface TransactionStrategy {

    CatalogMovementType supports();

    void validate(Account account, BigDecimal amount);

    BigDecimal apply(Account account, BigDecimal amount);
}
