package hn.infatlan.msvc_transactions.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import hn.infatlan.msvc_transactions.util.RequestHeaders;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    public static final String AUDITOR_ATTRIBUTE = "auditorUser";

    @Bean
    AuditorAware<String> auditorAware() {
        return () -> {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                Object auditor = servletAttributes.getAttribute(AUDITOR_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
                if (auditor instanceof String auditorValue && !auditorValue.isBlank()) {
                    return Optional.of(auditorValue);
                }

                String headerUser = servletAttributes.getRequest().getHeader(RequestHeaders.IDENTIFIER_USER);
                if (headerUser != null && !headerUser.isBlank()) {
                    return Optional.of(headerUser.trim());
                }
            }
            return Optional.of("SYSTEM");
        };
    }
}
