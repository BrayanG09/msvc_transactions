package hn.infatlan.msvc_transactions.enums;

import org.springframework.http.HttpStatus;

import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ValidationCode implements CodeCatalog {
    EXTERNAL_APPROVED("VAL_200", HttpStatus.OK, "External Approved",
            "La validación externa aprobó la operación."),
    EXTERNAL_REJECTED("VAL_422", HttpStatus.UNPROCESSABLE_CONTENT, "External Rejected",
            "El servicio de validación externa rechazó la operación."),
    EXTERNAL_VALIDATION_UNAVAILABLE("VAL_503", HttpStatus.SERVICE_UNAVAILABLE, "Validation Unavailable",
            "El servicio de validación externa no está disponible. La operación fue rechazada de forma controlada."),
    CIRCUIT_OPEN("VAL_503", HttpStatus.SERVICE_UNAVAILABLE, "Circuit Open",
            "El circuit breaker está abierto debido a fallos del servicio externo. Intente más tarde.");

    private final String code;
    private final HttpStatus httpCode;
    private final String message;
    private final String description;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public HttpStatus httpCode() {
        return httpCode;
    }
}
