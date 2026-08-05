package hn.infatlan.msvc_transactions.enums;

import org.springframework.http.HttpStatus;

import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountCode implements CodeCatalog {
        ACCOUNT_CREATED("ACC_201", HttpStatus.CREATED, "Account Created",
                        "La cuenta fue creada exitosamente."),
        ACCOUNT_FOUND("ACC_200", HttpStatus.OK, "Account Found",
                        "La cuenta fue consultada exitosamente."),
        ACCOUNT_NOT_FOUND("ACC_404", HttpStatus.NOT_FOUND, "Account Not Found",
                        "La cuenta solicitada no existe."),
        ACCOUNT_BLOCKED("ACC_403", HttpStatus.FORBIDDEN, "Account Blocked",
                        "La cuenta se encuentra bloqueada y no permite operaciones."),
        ACCOUNT_CLOSED("ACC_410", HttpStatus.GONE, "Account Closed",
                        "La cuenta se encuentra cerrada."),
        INVALID_INITIAL_BALANCE("ACC_400", HttpStatus.BAD_REQUEST, "Invalid Initial Balance",
                        "El saldo inicial debe ser mayor o igual a cero."),
        ACCOUNT_NUMBER_GENERATION_FAILED("ACC_500", HttpStatus.INTERNAL_SERVER_ERROR, "Account Number Generation Failed",
                        "No fue posible generar un número de cuenta único. Intente nuevamente."),
        CLIENT_INACTIVE("ACC_403", HttpStatus.FORBIDDEN, "Client Inactive",
                        "El cliente no se encuentra activo y no puede crear cuentas.");

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
