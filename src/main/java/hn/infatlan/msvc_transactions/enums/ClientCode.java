package hn.infatlan.msvc_transactions.enums;

import org.springframework.http.HttpStatus;

import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClientCode implements CodeCatalog {
    CLIENT_CREATED("CLT_201", HttpStatus.CREATED, "Client Created",
            "El cliente fue creado exitosamente."),
    CLIENT_FOUND("CLT_200", HttpStatus.OK, "Client Found",
            "El cliente fue consultado exitosamente."),
    CLIENT_NOT_FOUND("CLT_404", HttpStatus.NOT_FOUND, "Client Not Found",
            "El cliente solicitado no existe."),
    CLIENT_INACTIVE("CLT_403", HttpStatus.FORBIDDEN, "Client Inactive",
            "El cliente se encuentra inactivo y no permite operaciones."),
    INVALID_IDENTITY("CLT_400", HttpStatus.BAD_REQUEST, "Invalid Identity",
            "El número de identidad proporcionado no es válido."),
    DUPLICATE_EMAIL("CLT_409", HttpStatus.CONFLICT, "Duplicate Email",
            "El correo electrónico ya está registrado para otro cliente.");

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
