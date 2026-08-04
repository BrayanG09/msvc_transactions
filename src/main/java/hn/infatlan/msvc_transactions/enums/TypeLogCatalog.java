package hn.infatlan.msvc_transactions.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeLogCatalog {
    ACCOUNT("ACCOUNT", "Este tipo de log se produce en el modulo de cuentas.");

    private final String value;
    private final String description;
}
