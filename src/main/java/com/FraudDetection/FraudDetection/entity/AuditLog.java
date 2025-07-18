package com.FraudDetection.FraudDetection.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_log_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_audit_log_fraud_alert_id", columnList = "fraud_alert_id"),
    @Index(name = "idx_audit_log_action", columnList = "action"),
    @Index(name = "idx_audit_log_created_at", columnList = "createdAt"),
    @Index(name = "idx_audit_log_user", columnList = "performedBy"),
    @Index(name = "idx_audit_log_entity_type", columnList = "entityType")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"transaction", "fraudAlert"})
@Schema(description = "Audit log entity for tracking all system activities")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique audit log ID", example = "1")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    @Schema(description = "Associated transaction (if applicable)")
    private Transaction transaction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraud_alert_id")
    @Schema(description = "Associated fraud alert (if applicable)")
    private FraudAlert fraudAlert;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Entity type is required")
    @Size(max = 50, message = "Entity type must not exceed 50 characters")
    @Schema(description = "Type of entity being audited", example = "TRANSACTION")
    private String entityType;
    
    @Column(length = 50)
    @Size(max = 50, message = "Entity ID must not exceed 50 characters")
    @Schema(description = "ID of the entity being audited", example = "TXN-001234")
    private String entityId;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Action is required")
    @Size(max = 50, message = "Action must not exceed 50 characters")
    @Schema(description = "Action performed", example = "CREATE")
    private String action;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Action description is required")
    @Size(max = 100, message = "Action description must not exceed 100 characters")
    @Schema(description = "Description of the action", example = "Transaction created successfully")
    private String actionDescription;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Performed by is required")
    @Size(max = 50, message = "Performed by must not exceed 50 characters")
    @Schema(description = "User who performed the action", example = "system_user")
    private String performedBy;
    
    @Column(length = 100)
    @Size(max = 100, message = "User role must not exceed 100 characters")
    @Schema(description = "Role of the user who performed the action", example = "FRAUD_ANALYST")
    private String userRole;
    
    @Column(length = 50)
    @Size(max = 50, message = "Source system must not exceed 50 characters")
    @Schema(description = "System or source of the action", example = "WEB_APP")
    private String sourceSystem;
    
    @Column(length = 45)
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Schema(description = "IP address from which action was performed", example = "192.168.1.100")
    private String ipAddress;
    
    @Column(length = 200)
    @Size(max = 200, message = "User agent must not exceed 200 characters")
    @Schema(description = "User agent string", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;
    
    @Column(columnDefinition = "TEXT")
    @Schema(description = "Previous values before the change (JSON format)")
    private String previousValues;
    
    @Column(columnDefinition = "TEXT")
    @Schema(description = "New values after the change (JSON format)")
    private String newValues;
    
    @Column(length = 1000)
    @Size(max = 1000, message = "Additional details must not exceed 1000 characters")
    @Schema(description = "Additional details about the action", example = "Fraud rule triggered: velocity_check")
    private String additionalDetails;
    
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Severity level of the audit event", example = "INFO")
    private AuditSeverity severity = AuditSeverity.INFO;
    
    @Column(length = 50)
    @Size(max = 50, message = "Event category must not exceed 50 characters")
    @Schema(description = "Category of the audit event", example = "FRAUD_DETECTION")
    private String eventCategory;
    
    @Column(length = 100)
    @Size(max = 100, message = "Tags must not exceed 100 characters")
    @Schema(description = "Tags for categorizing the event", example = "high_risk,manual_review")
    private String tags;
    
    @Column
    @Schema(description = "Whether this action was successful", example = "true")
    private Boolean successful = true;
    
    @Column(length = 500)
    @Size(max = 500, message = "Error message must not exceed 500 characters")
    @Schema(description = "Error message if action failed", example = "Database connection timeout")
    private String errorMessage;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the audit event occurred", example = "2025-07-18 10:30:00")
    private LocalDateTime createdAt;
    
    // JPA Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}