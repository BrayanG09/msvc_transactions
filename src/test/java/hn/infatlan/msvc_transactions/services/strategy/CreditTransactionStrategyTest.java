package hn.infatlan.msvc_transactions.services.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hn.infatlan.msvc_transactions.entities.Account;
import hn.infatlan.msvc_transactions.enums.AccountCode;
import hn.infatlan.msvc_transactions.enums.CatalogAccountStatus;
import hn.infatlan.msvc_transactions.exceptions.InfatlanTransactionException;
import hn.infatlan.msvc_transactions.support.CatalogFixtures;

class CreditTransactionStrategyTest {

    private final CreditTransactionStrategy strategy = new CreditTransactionStrategy();
    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .balance(new BigDecimal("50.0000"))
                .currency("HNL")
                .status(CatalogFixtures.accountStatus(CatalogAccountStatus.ACTIVE))
                .build();
    }

    @Test
    void apply_addsAmountToBalance() {
        BigDecimal result = strategy.apply(account, new BigDecimal("25.5000"));

        assertThat(result).isEqualByComparingTo("75.5000");
        assertThat(account.getBalance()).isEqualByComparingTo("75.5000");
    }

    @Test
    void validate_allowsActiveAccount() {
        strategy.validate(account, new BigDecimal("10.0000"));
    }

    @Test
    void validate_rejectsBlockedAccount() {
        account.setStatus(CatalogFixtures.accountStatus(CatalogAccountStatus.BLOCKED));

        assertThatThrownBy(() -> strategy.validate(account, new BigDecimal("10.0000")))
                .isInstanceOf(InfatlanTransactionException.class)
                .extracting(ex -> ((InfatlanTransactionException) ex).getCodeCatalog())
                .isEqualTo(AccountCode.ACCOUNT_BLOCKED);
    }
}
