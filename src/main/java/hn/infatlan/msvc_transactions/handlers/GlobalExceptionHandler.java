package hn.infatlan.msvc_transactions.handlers;

import org.springframework.web.bind.annotation.ControllerAdvice;

import lombok.RequiredArgsConstructor;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import hn.infatlan.msvc_transactions.dtos.common.ResponseDTO;
import hn.infatlan.msvc_transactions.enums.ResponseCodeCatalog;
import hn.infatlan.msvc_transactions.exceptions.InfatlanTransactionException;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.INVALID_PARAMETERS;

        String messageError = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> ex.getBindingResult().getGlobalErrors().stream()
                        .map(ObjectError::getDefaultMessage)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(codeCatalog.description()));

        ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>())
                .withCustomMessage(messageError);

        return ResponseEntity
                .status(codeCatalog.getHttpCode())
                .body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.MISSING_REQUEST_HEADER;

        ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>())
                .withCustomMessage(ex.getMessage());

        return ResponseEntity
                .status(codeCatalog.getHttpCode())
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.BAD_REQUEST;

        ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

        return ResponseEntity.status(codeCatalog.getHttpCode())
                .body(response);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<Object> handleTimeoutException(TimeoutException ex) {
        ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.REQUEST_TIMEOUT;

        ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

        return ResponseEntity.status(codeCatalog.getHttpCode())
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.FORBIDDEN;

        ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

        return ResponseEntity.status(codeCatalog.getHttpCode())
                .body(response);
    }

    /**
     * Validar de @RequestParam y @PathVariable
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.INVALID_PARAMETERS;

        ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

        return ResponseEntity.status(codeCatalog.getHttpCode())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.INTERNAL_SERVER_ERROR;

        ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

        return ResponseEntity.status(codeCatalog.getHttpCode())
                .body(response);
    }

    @ExceptionHandler(InfatlanTransactionException.class)
    public ResponseEntity<Object> handleFicohsaInsuranceException(InfatlanTransactionException ex) {
        ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(ex.getCodeCatalog(), new HashMap<>());

        if (!Objects.isNull(ex.getWithCustomMessage()) && !ex.getWithCustomMessage().isBlank()) {
            response.withCustomMessage(ex.getWithCustomMessage());
        }

        return ResponseEntity.status(ex.getCodeCatalog().httpCode())
                .body(response);
    }
}
