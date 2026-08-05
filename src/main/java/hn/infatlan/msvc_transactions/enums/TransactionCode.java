package hn.infatlan.msvc_transactions.enums;

import org.springframework.http.HttpStatus;

import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionCode implements CodeCatalog {
        TRANSACTION_CONFIRMED("TR_200", HttpStatus.OK, "Transaction Confirmed",
                        "El movimiento fue confirmado exitosamente."),
        INSUFFICIENT_FUNDS("TR_422", HttpStatus.CONFLICT, "Insufficient Funds",
                        "El saldo de la cuenta es insuficiente para realizar el débito."),
        INVALID_MOVEMENT_TYPE("TR_400", HttpStatus.BAD_REQUEST, "Invalid Movement Type",
                        "El tipo de movimiento no es válido. Use CREDIT o DEBIT."),
        IDEMPOTENCY_KEY_REQUIRED("TR_400", HttpStatus.BAD_REQUEST, "Idempotency Key Required",
                        "El encabezado Idempotency-Key es obligatorio."),
        IDEMPOTENCY_CONFLICT("TR_409", HttpStatus.CONFLICT, "Idempotency Conflict",
                        "La clave de idempotencia ya fue utilizada con un cuerpo de solicitud diferente."),
        CONCURRENT_MODIFICATION("TR_409", HttpStatus.CONFLICT, "Concurrent Modification",
                        "La cuenta fue modificada por otra operación. Intente nuevamente.");

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
