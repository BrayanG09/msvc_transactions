package hn.infatlan.msvc_transactions.enums;

import org.springframework.http.HttpStatus;

import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCodeCatalog implements CodeCatalog {
    SUCCESS("API_200", HttpStatus.OK, "Success", "Operación realizada con éxito."),
    BAD_REQUEST("API_400", HttpStatus.BAD_REQUEST, "Bad Request",
            "La solicitud no pudo ser procesada debido a un error del cliente, como una sintaxis de solicitud mal formada o parámetros inválidos."),
    UNAUTHORIZED("API_401", HttpStatus.UNAUTHORIZED, "Unauthorized",
            "La solicitud requiere autenticación del usuario. El cliente debe proporcionar credenciales válidas para acceder al recurso solicitado."),
    FORBIDDEN("API_403", HttpStatus.FORBIDDEN, "Forbidden",
            "El servidor entiende la solicitud, pero se niega a autorizarla. El cliente no tiene permisos suficientes para acceder al recurso solicitado."),
    NOT_FOUND("API_404", HttpStatus.NOT_FOUND, "Not Found",
            "El recurso solicitado no se pudo encontrar en el servidor. Puede deberse a una URL incorrecta o a que el recurso ha sido eliminado."),
    INTERNAL_SERVER_ERROR("API_500", HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "El servidor encontró un error interno al procesar la solicitud. Esto puede deberse a un problema en el código del servidor o a una falla temporal en el sistema."),

    // Códigos generales
    INVALID_PARAMETERS("API_700", HttpStatus.BAD_REQUEST, "Invalid Parameters",
            "La solicitud contiene parámetros inválidos o no cumple con los requisitos esperados. El servidor no puede procesar la solicitud debido a la falta de información válida o a la presencia de datos incorrectos."),
    MISSING_REQUEST_HEADER("API_701", HttpStatus.BAD_REQUEST, "Missing Request Header",
            "La solicitud no contiene un encabezado requerido. El cliente debe incluir el encabezado necesario para que el servidor pueda procesar la solicitud correctamente."),
    REQUEST_TIMEOUT("API_702", HttpStatus.REQUEST_TIMEOUT, "Request Timeout",
            "El servidor no recibió una solicitud completa dentro del tiempo permitido. Esto puede deberse a una conexión lenta o a un retraso en la transmisión de datos. El cliente puede intentar enviar la solicitud nuevamente.");

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
