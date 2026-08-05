package hn.infatlan.msvc_transactions.services.implementations;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.stereotype.Service;

import hn.infatlan.msvc_transactions.entities.ApplicationLog;
import hn.infatlan.msvc_transactions.enums.LevelLogCatalog;
import hn.infatlan.msvc_transactions.enums.ProjectsCatalog;
import hn.infatlan.msvc_transactions.enums.TypeLogCatalog;
import hn.infatlan.msvc_transactions.exceptions.InfatlanTransactionException;
import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import hn.infatlan.msvc_transactions.services.definitions.ApplicationLogService;
import hn.infatlan.msvc_transactions.util.CorrelationContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationLogServiceImpl implements ApplicationLogService {

    private final ApplicationLogAsyncWriter applicationLogAsyncWriter;

    @Override
    public void logBusinessException(InfatlanTransactionException exception, String path) {
        CodeCatalog codeCatalog = exception.getCodeCatalog();
        Throwable technicalException = resolveTechnicalException(exception);

        ApplicationLog.ApplicationLogBuilder logBuilder = ApplicationLog.builder()
                .correlationId(CorrelationContext.getCorrelationId())
                .project(resolveProject(exception))
                .type(exception.getType() != null ? exception.getType().getValue() : TypeLogCatalog.BUSINESS.getValue())
                .process(exception.getProcess() != null ? exception.getProcess().getValue() : null)
                .level(exception.getLevel() != null ? exception.getLevel().getValue() : LevelLogCatalog.WARN.getValue())
                .code(codeCatalog != null ? codeCatalog.code() : null)
                .message(resolveMessage(exception, codeCatalog))
                .description(codeCatalog != null ? codeCatalog.description() : null)
                .httpCode(codeCatalog != null ? codeCatalog.httpCode().value() : null)
                .userIdentifier(firstNonBlank(exception.getUserIdentifier(), CorrelationContext.getUserIdentifier()))
                .metadata(exception.getMetadata())
                .path(firstNonBlank(exception.getPath(), path));

        if (technicalException != null) {
            logBuilder
                    .exceptionClass(technicalException.getClass().getName())
                    .exceptionMessage(technicalException.getMessage())
                    .exceptionCauseClass(causeClass(technicalException))
                    .exceptionCauseMessage(causeMessage(technicalException))
                    .exceptionStackTrace(stackTrace(technicalException));
        }

        applicationLogAsyncWriter.persist(logBuilder.build());
    }

    @Override
    public void logUnhandledException(Exception exception, CodeCatalog codeCatalog, String path) {
        ApplicationLog log = ApplicationLog.builder()
                .correlationId(CorrelationContext.getCorrelationId())
                .project(ProjectsCatalog.MSVC_TRANSACTIONS.getValue())
                .type(TypeLogCatalog.TECHNICAL.getValue())
                .process(null)
                .level(LevelLogCatalog.ERROR.getValue())
                .code(codeCatalog != null ? codeCatalog.code() : null)
                .message(codeCatalog != null ? codeCatalog.message() : exception.getMessage())
                .description(codeCatalog != null ? codeCatalog.description() : null)
                .httpCode(codeCatalog != null ? codeCatalog.httpCode().value() : null)
                .userIdentifier(CorrelationContext.getUserIdentifier())
                .path(path)
                .exceptionClass(exception.getClass().getName())
                .exceptionMessage(exception.getMessage())
                .exceptionCauseClass(causeClass(exception))
                .exceptionCauseMessage(causeMessage(exception))
                .exceptionStackTrace(stackTrace(exception))
                .build();

        applicationLogAsyncWriter.persist(log);
    }

    private static String resolveProject(InfatlanTransactionException exception) {
        return exception.getProject() != null
                ? exception.getProject().getValue()
                : ProjectsCatalog.MSVC_TRANSACTIONS.getValue();
    }

    private static String resolveMessage(InfatlanTransactionException exception, CodeCatalog codeCatalog) {
        if (exception.getWithCustomMessage() != null && !exception.getWithCustomMessage().isBlank()) {
            return exception.getWithCustomMessage();
        }
        return codeCatalog != null ? codeCatalog.message() : null;
    }

    /**
     * Solo se persisten datos de excepción cuando hay una causa técnica
     * distinta de {@link InfatlanTransactionException}.
     */
    private static Throwable resolveTechnicalException(InfatlanTransactionException exception) {
        Exception wrapped = exception.getException();
        if (wrapped == null || wrapped instanceof InfatlanTransactionException) {
            return null;
        }
        return wrapped;
    }

    private static String causeClass(Throwable throwable) {
        if (throwable == null || throwable.getCause() == null) {
            return null;
        }
        return throwable.getCause().getClass().getName();
    }

    private static String causeMessage(Throwable throwable) {
        if (throwable == null || throwable.getCause() == null) {
            return null;
        }
        return throwable.getCause().getMessage();
    }

    private static String stackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return null;
    }
}
