package hn.infatlan.msvc_transactions.dtos.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {

    private UUID id;
    private String accountNumber;
    private UUID clientId;
    private String identityNumber;
    private String fullName;
    private BigDecimal balance;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
