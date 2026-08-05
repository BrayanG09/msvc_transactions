package hn.infatlan.msvc_transactions.adapters.validation;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import hn.infatlan.msvc_transactions.enums.ProcessLogCatalog;
import hn.infatlan.msvc_transactions.enums.TypeLogCatalog;
import hn.infatlan.msvc_transactions.enums.ValidationCode;
import hn.infatlan.msvc_transactions.util.ExceptionFactory;
import hn.infatlan.msvc_transactions.util.RequestHeaders;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ResilientValidationAdapter implements ValidationPort {

    private static final String INSTANCE_NAME = "validationService";

    private final RestClient validationRestClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ResilientValidationAdapter(
            @Qualifier("validationRestClient") RestClient validationRestClient,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry) {
        this.validationRestClient = validationRestClient;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(INSTANCE_NAME);
        this.retry = retryRegistry.retry(INSTANCE_NAME);
    }

    @Override
    public ValidationResult authorize(ValidationRequest request) {
        Supplier<ValidationResult> decorated = Decorators
                .ofSupplier(() -> callExternalService(request))
                .withCircuitBreaker(circuitBreaker)
                .withRetry(retry)
                .decorate();

        try {
            return decorated.get();
        } catch (CallNotPermittedException ex) {
            log.warn("Circuit breaker OPEN for validation service");
            throw ExceptionFactory.business(
                    ValidationCode.CIRCUIT_OPEN,
                    ProcessLogCatalog.EXTERNAL_VALIDATION,
                    TypeLogCatalog.VALIDATION,
                    null);
        } catch (RuntimeException ex) {
            if (ex instanceof CallNotPermittedException) {
                throw ex;
            }
            log.warn("External validation unavailable. cause={}", ex.getClass().getSimpleName());
            throw ExceptionFactory.business(
                    ValidationCode.EXTERNAL_VALIDATION_UNAVAILABLE,
                    ProcessLogCatalog.EXTERNAL_VALIDATION,
                    TypeLogCatalog.VALIDATION,
                    null);
        }
    }

    private ValidationResult callExternalService(ValidationRequest request) {
        log.info("Calling external validation service. accountId={}, type={}, amount={}",
                request.accountId(), request.type(), request.amount());

        ExternalValidationResponse response = validationRestClient.post()
                .uri("/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestHeaders.CORRELATION_ID,
                        request.correlationId() == null ? "" : request.correlationId())
                .body(ExternalValidationRequest.from(request))
                .retrieve()
                .body(ExternalValidationResponse.class);

        if (response == null) {
            throw new RestClientException("Empty response from validation service");
        }

        return ValidationResult.builder()
                .approved(response.approved())
                .authCode(response.authCode())
                .reason(response.reason())
                .build();
    }

    private record ExternalValidationRequest(
            String accountId,
            String type,
            String amount,
            String currency,
            String correlationId) {

        static ExternalValidationRequest from(ValidationRequest request) {
            return new ExternalValidationRequest(
                    request.accountId().toString(),
                    request.type(),
                    request.amount().toPlainString(),
                    request.currency(),
                    request.correlationId());
        }
    }

    private record ExternalValidationResponse(
            boolean approved,
            String authCode,
            String reason) {
    }
}
