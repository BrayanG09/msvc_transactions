package hn.infatlan.msvc_transactions.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProcessLogCatalog {
    CREATE_ACCOUNT("CREATE_ACCOUNT", "Creación de cuenta"),
    GET_ACCOUNT("GET_ACCOUNT", "Consulta de cuenta"),
    FIND_OR_CREATE_CLIENT("FIND_OR_CREATE_CLIENT", "Búsqueda o creación de cliente"),
    CREATE_TRANSACTION("CREATE_TRANSACTION", "Registro de movimiento"),
    GET_STATEMENT("GET_STATEMENT", "Generación de estado de cuenta"),
    EXTERNAL_VALIDATION("EXTERNAL_VALIDATION", "Validación con servicio externo");

    private final String value;
    private final String description;
}
