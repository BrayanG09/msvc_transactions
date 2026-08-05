package hn.infatlan.msvc_transactions.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeLogCatalog {
    ACCOUNT("ACCOUNT", "Este tipo de log se produce en el modulo de cuentas."),
    TRANSACTION("TRANSACTION", "Este tipo de log se produce en el modulo de movimientos."),
    STATEMENT("STATEMENT", "Este tipo de log se produce en el modulo de estados de cuenta."),
    VALIDATION("VALIDATION", "Este tipo de log se produce en la validación externa."),
    BUSINESS("BUSINESS", "Error o evento de regla de negocio."),
    TECHNICAL("TECHNICAL", "Error o evento técnico del sistema.");

    private final String value;
    private final String description;
}
