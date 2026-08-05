package hn.infatlan.msvc_transactions.adapters.validation;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ValidationRequest(
        UUID accountId,
        String type,
        BigDecimal amount,
        String currency,
        String correlationId) {
}
