package hn.infatlan.msvc_transactions.adapters.validation;

import lombok.Builder;

@Builder
public record ValidationResult(
        boolean approved,
        String authCode,
        String reason) {
}
