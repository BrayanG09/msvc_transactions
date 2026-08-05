package hn.infatlan.msvc_transactions.filters;

import java.io.IOException;
import java.util.UUID;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import hn.infatlan.msvc_transactions.util.CorrelationContext;
import hn.infatlan.msvc_transactions.util.RequestHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(RequestHeaders.CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        CorrelationContext.setCorrelationId(correlationId);
        CorrelationContext.setUserIdentifier(request.getHeader(RequestHeaders.IDENTIFIER_USER));
        response.setHeader(RequestHeaders.CORRELATION_ID, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            CorrelationContext.clear();
        }
    }
}
