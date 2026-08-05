package hn.infatlan.msvc_transactions.services.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hn.infatlan.msvc_transactions.entities.Account;
import hn.infatlan.msvc_transactions.enums.CatalogAccountStatus;
import hn.infatlan.msvc_transactions.enums.TransactionCode;
import hn.infatlan.msvc_transactions.exceptions.InfatlanTransactionException;
import hn.infatlan.msvc_transactions.support.CatalogFixtures;

class DebitTransactionStrategyTest {

    private final DebitTransactionStrategy strategy = new DebitTransactionStrategy();
    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .balance(new BigDecimal("100.0000"))
                .currency("HNL")
                .status(CatalogFixtures.accountStatus(CatalogAccountStatus.ACTIVE))
                .build();
    }

    @Test
    void apply_subtractsAmountFromBalance() {
        BigDecimal result = strategy.apply(account, new BigDecimal("40.0000"));

        assertThat(result).isEqualByComparingTo("60.0000");
        assertThat(account.getBalance()).isEqualByComparingTo("60.0000");
    }

    @Test
    void validate_rejectsInsufficientFunds() {
        assertThatThrownBy(() -> strategy.validate(account, new BigDecimal("150.0000")))
                .isInstanceOf(InfatlanTransactionException.class)
                .extracting(ex -> ((InfatlanTransactionException) ex).getCodeCatalog())
                .isEqualTo(TransactionCode.INSUFFICIENT_FUNDS);
    }
}
