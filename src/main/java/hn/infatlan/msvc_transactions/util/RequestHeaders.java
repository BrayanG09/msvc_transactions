package hn.infatlan.msvc_transactions.util;

public final class RequestHeaders {

    public static final String IDENTIFIER_USER = "identifier-user";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private RequestHeaders() {
    }
}
