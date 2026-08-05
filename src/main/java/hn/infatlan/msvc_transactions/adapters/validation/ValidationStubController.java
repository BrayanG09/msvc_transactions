package hn.infatlan.msvc_transactions.adapters.validation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Hidden
@RestController
@RequestMapping("/external")
@ConditionalOnProperty(name = "validation.stub.enabled", havingValue = "true", matchIfMissing = true)
public class ValidationStubController {

    private final AtomicBoolean available = new AtomicBoolean(true);

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody Map<String, Object> request) {
        if (!available.get()) {
            log.warn("Validation stub is DOWN. Rejecting request.");
            return ResponseEntity.status(503).body(Map.of(
                    "approved", false,
                    "reason", "Validation stub is unavailable"));
        }

        String authCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Validation stub APPROVED. authCode={}", authCode);
        return ResponseEntity.ok(Map.of(
                "approved", true,
                "authCode", authCode,
                "reason", ""));
    }

    @PostMapping("/admin/down")
    public ResponseEntity<Map<String, Object>> takeDown() {
        available.set(false);
        return ResponseEntity.ok(Map.of("available", false, "message", "Stub apagado"));
    }

    @PostMapping("/admin/up")
    public ResponseEntity<Map<String, Object>> bringUp() {
        available.set(true);
        return ResponseEntity.ok(Map.of("available", true, "message", "Stub encendido"));
    }

    @GetMapping("/admin/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of("available", available.get()));
    }
}
