package hn.infatlan.msvc_transactions.dtos.statement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementResponseDTO {

    private UUID accountId;
    private String accountNumber;
    private LocalDate from;
    private LocalDate to;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private List<StatementMovementItemDTO> movements;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
