package hn.infatlan.msvc_transactions.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectsCatalog {
    MSVC_TRANSACTIONS("MSVC_TRANSACTIONS", "Microservicio de transacciones");

    private final String value;
    private final String description;
}
