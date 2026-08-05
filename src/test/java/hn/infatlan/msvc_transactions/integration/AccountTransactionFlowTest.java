package hn.infatlan.msvc_transactions.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import hn.infatlan.msvc_transactions.adapters.validation.ValidationPort;
import hn.infatlan.msvc_transactions.adapters.validation.ValidationResult;
import hn.infatlan.msvc_transactions.repositories.AccountRepository;
import hn.infatlan.msvc_transactions.repositories.AccountStatusRepository;
import hn.infatlan.msvc_transactions.repositories.ClientStatusRepository;
import hn.infatlan.msvc_transactions.repositories.MovementRepository;
import hn.infatlan.msvc_transactions.repositories.MovementStatusRepository;
import hn.infatlan.msvc_transactions.repositories.MovementTypeRepository;
import hn.infatlan.msvc_transactions.repositories.StatementProcedureRepository;
import hn.infatlan.msvc_transactions.support.CatalogFixtures;
import hn.infatlan.msvc_transactions.util.RequestHeaders;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountTransactionFlowTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private AccountStatusRepository accountStatusRepository;
        @Autowired
        private ClientStatusRepository clientStatusRepository;
        @Autowired
        private MovementTypeRepository movementTypeRepository;
        @Autowired
        private MovementStatusRepository movementStatusRepository;
        @Autowired
        private AccountRepository accountRepository;
        @Autowired
        private MovementRepository movementRepository;

        @MockitoBean
        private ValidationPort validationPort;

        @MockitoBean
        private StatementProcedureRepository statementProcedureRepository;

        @BeforeEach
        void setUp() {
                CatalogFixtures.seedAll(
                                accountStatusRepository,
                                clientStatusRepository,
                                movementTypeRepository,
                                movementStatusRepository);
                when(validationPort.authorize(any())).thenReturn(ValidationResult.builder()
                                .approved(true)
                                .authCode("AUTH-IT")
                                .build());
        }

        @Test
        void createAccount_getAccount_andDebitTransaction_flow() throws Exception {
                MvcResult createResult = mockMvc.perform(post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "identityNumber": "0801199011111",
                                                  "fullName": "Maria Lopez",
                                                  "email": "maria@example.com",
                                                  "initialBalance": 150.0000
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.code").value("ACC_201"))
                                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                                .andExpect(jsonPath("$.data.balance").value(150.0))
                                .andReturn();

                String accountId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");

                mockMvc.perform(get("/accounts/{id}", accountId)
                                .header(RequestHeaders.IDENTIFIER_USER, "tester"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value("ACC_200"))
                                .andExpect(jsonPath("$.data.id").value(accountId));

                mockMvc.perform(post("/accounts/{accountId}/transactions", accountId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(RequestHeaders.IDENTIFIER_USER, "tester")
                                .header(RequestHeaders.IDEMPOTENCY_KEY, "it-debit-1")
                                .content("""
                                                {
                                                  "type": "DEBIT",
                                                  "amount": 50.0000,
                                                  "description": "Retiro IT"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value("TR_200"))
                                .andExpect(jsonPath("$.data.balanceAfter").value(100.0))
                                .andExpect(jsonPath("$.data.externalAuthRef").value("AUTH-IT"));

                assertThat(accountRepository.findById(java.util.UUID.fromString(accountId)))
                                .isPresent()
                                .get()
                                .extracting(account -> account.getBalance())
                                .satisfies(balance -> assertThat((BigDecimal) balance)
                                                .isEqualByComparingTo("100.0000"));

                assertThat(movementRepository.count()).isGreaterThanOrEqualTo(2);
        }
}
