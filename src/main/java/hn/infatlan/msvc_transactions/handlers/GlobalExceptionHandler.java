package hn.infatlan.msvc_transactions.handlers;

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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import hn.infatlan.msvc_transactions.dtos.common.ResponseDTO;
import hn.infatlan.msvc_transactions.enums.ResponseCodeCatalog;
import hn.infatlan.msvc_transactions.exceptions.InfatlanTransactionException;
import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import hn.infatlan.msvc_transactions.services.definitions.ApplicationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        private final ApplicationLogService applicationLogService;

        @Override
        protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpHeaders headers,
                        HttpStatusCode status,
                        WebRequest request) {
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

                logUnhandled(ex, codeCatalog, request);

                return ResponseEntity
                                .status(codeCatalog.getHttpCode())
                                .body(response);
        }

        @ExceptionHandler(MissingRequestHeaderException.class)
        public ResponseEntity<Object> handleMissingRequestHeader(
                        MissingRequestHeaderException ex,
                        HttpServletRequest request) {
                ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.MISSING_REQUEST_HEADER;

                ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>())
                                .withCustomMessage(ex.getMessage());

                logUnhandled(ex, codeCatalog, request);

                return ResponseEntity
                                .status(codeCatalog.getHttpCode())
                                .body(response);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Object> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {
                ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.BAD_REQUEST;

                ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

                logUnhandled(ex, codeCatalog, request);

                return ResponseEntity.status(codeCatalog.getHttpCode())
                                .body(response);
        }

        @ExceptionHandler(TimeoutException.class)
        public ResponseEntity<Object> handleTimeoutException(TimeoutException ex, HttpServletRequest request) {
                ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.REQUEST_TIMEOUT;

                ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

                logUnhandled(ex, codeCatalog, request);

                return ResponseEntity.status(codeCatalog.getHttpCode())
                                .body(response);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex,
                        HttpServletRequest request) {
                ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.FORBIDDEN;

                ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

                logUnhandled(ex, codeCatalog, request);

                return ResponseEntity.status(codeCatalog.getHttpCode())
                                .body(response);
        }

        /**
         * Validaciones de @RequestParam, @PathVariable y @RequestHeader
         */
        @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
        public ResponseEntity<Object> handleJakartaConstraintViolationException(
                        jakarta.validation.ConstraintViolationException ex,
                        HttpServletRequest request) {
                ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.INVALID_PARAMETERS;

                String messageError = ex.getConstraintViolations().stream()
                                .map(jakarta.validation.ConstraintViolation::getMessage)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(codeCatalog.description());

                ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>())
                                .withCustomMessage(messageError);

                logUnhandled(ex, codeCatalog, request);

                return ResponseEntity.status(codeCatalog.getHttpCode()).body(response);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Object> handleConstraintViolationException(
                        ConstraintViolationException ex,
                        HttpServletRequest request) {
                ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.INVALID_PARAMETERS;

                ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

                logUnhandled(ex, codeCatalog, request);

                return ResponseEntity.status(codeCatalog.getHttpCode())
                                .body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Object> handleGenericException(Exception ex, HttpServletRequest request) {
                ResponseCodeCatalog codeCatalog = ResponseCodeCatalog.INTERNAL_SERVER_ERROR;

                ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(codeCatalog, new HashMap<>());

                logUnhandled(ex, codeCatalog, request);

                return ResponseEntity.status(codeCatalog.getHttpCode())
                                .body(response);
        }

        @ExceptionHandler(InfatlanTransactionException.class)
        public ResponseEntity<Object> handleInfatlanTransactionException(
                        InfatlanTransactionException ex,
                        HttpServletRequest request) {
                ResponseDTO<HashMap<Object, Object>> response = ResponseDTO.error(ex.getCodeCatalog(), new HashMap<>());

                if (!Objects.isNull(ex.getWithCustomMessage()) && !ex.getWithCustomMessage().isBlank()) {
                        response.withCustomMessage(ex.getWithCustomMessage());
                }

                applicationLogService.logBusinessException(ex, request.getRequestURI());

                return ResponseEntity.status(ex.getCodeCatalog().httpCode())
                                .body(response);
        }

        private void logUnhandled(Exception ex, CodeCatalog codeCatalog, WebRequest request) {
                applicationLogService.logUnhandledException(ex, codeCatalog, request.getDescription(false));
        }

        private void logUnhandled(Exception ex, CodeCatalog codeCatalog, HttpServletRequest request) {
                applicationLogService.logUnhandledException(ex, codeCatalog, request.getRequestURI());
        }
}
