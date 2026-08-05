package hn.infatlan.msvc_transactions.util;

import org.slf4j.MDC;

public final class CorrelationContext {

    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_USER_IDENTIFIER = "identifierUser";

    private CorrelationContext() {
    }

    public static String getCorrelationId() {
        return MDC.get(MDC_CORRELATION_ID);
    }

    public static String getUserIdentifier() {
        return MDC.get(MDC_USER_IDENTIFIER);
    }

    public static void setCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            MDC.remove(MDC_CORRELATION_ID);
            return;
        }
        MDC.put(MDC_CORRELATION_ID, correlationId);
    }

    public static void setUserIdentifier(String userIdentifier) {
        if (userIdentifier == null || userIdentifier.isBlank()) {
            MDC.remove(MDC_USER_IDENTIFIER);
            return;
        }
        MDC.put(MDC_USER_IDENTIFIER, userIdentifier);
    }

    public static void clear() {
        MDC.remove(MDC_CORRELATION_ID);
        MDC.remove(MDC_USER_IDENTIFIER);
    }
}
