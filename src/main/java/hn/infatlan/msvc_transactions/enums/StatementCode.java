package hn.infatlan.msvc_transactions.enums;

import org.springframework.http.HttpStatus;

import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatementCode implements CodeCatalog {
    STATEMENT_GENERATED("STM_200", HttpStatus.OK, "Statement Generated",
            "El estado de cuenta fue generado exitosamente."),
    INVALID_DATE_RANGE("STM_400", HttpStatus.BAD_REQUEST, "Invalid Date Range",
            "El rango de fechas no es válido. La fecha de inicio debe ser menor o igual a la fecha de fin."),
    INVALID_PAGE_SIZE("STM_400", HttpStatus.BAD_REQUEST, "Invalid Page Size",
            "El tamaño de página debe estar entre 1 y 100.");

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
