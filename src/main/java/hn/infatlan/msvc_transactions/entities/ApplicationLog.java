package hn.infatlan.msvc_transactions.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "application_logs")
public class ApplicationLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "log_id", nullable = false, updatable = false)
    private UUID logId;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "project", length = 100)
    private String project;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "process", length = 100)
    private String process;

    @Column(name = "level", length = 20)
    private String level;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "message", length = 255)
    private String message;

    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Column(name = "description")
    private String description;

    @Column(name = "http_code")
    private Integer httpCode;

    @Column(name = "user_identifier", length = 100)
    private String userIdentifier;

    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Column(name = "metadata")
    private String metadata;

    @Column(name = "path", length = 500)
    private String path;

    @Column(name = "exception_class", length = 255)
    private String exceptionClass;

    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Column(name = "exception_message")
    private String exceptionMessage;

    @Column(name = "exception_cause_class", length = 255)
    private String exceptionCauseClass;

    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Column(name = "exception_cause_message")
    private String exceptionCauseMessage;

    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Column(name = "exception_stack_trace")
    private String exceptionStackTrace;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
