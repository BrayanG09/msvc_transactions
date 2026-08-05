package hn.infatlan.msvc_transactions.services.implementations;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import hn.infatlan.msvc_transactions.config.AsyncConfig;
import hn.infatlan.msvc_transactions.entities.ApplicationLog;
import hn.infatlan.msvc_transactions.repositories.ApplicationLogRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationLogAsyncWriter {

    private final ApplicationLogRepository applicationLogRepository;
    private final MeterRegistry meterRegistry;

    @Async(AsyncConfig.APPLICATION_LOG_EXECUTOR)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persist(ApplicationLog applicationLog) {
        try {
            applicationLogRepository.save(applicationLog);
            meterRegistry.counter("application.logs.persisted", "level",
                    applicationLog.getLevel() != null ? applicationLog.getLevel() : "UNKNOWN").increment();
        } catch (Exception ex) {
            log.error("Failed to persist application log. correlationId={}", applicationLog.getCorrelationId(), ex);
            meterRegistry.counter("application.logs.persist.failures").increment();
        }
    }
}
