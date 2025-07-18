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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "fraud_alerts", indexes = {
    @Index(name = "idx_fraud_alert_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_fraud_alert_severity", columnList = "severity"),
    @Index(name = "idx_fraud_alert_status", columnList = "status"),
    @Index(name = "idx_fraud_alert_created_at", columnList = "createdAt"),
    @Index(name = "idx_fraud_alert_rule_type", columnList = "ruleType")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"transaction", "auditLogs"})
@Schema(description = "Fraud alert entity for flagged transactions")
public class FraudAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique fraud alert ID", example = "1")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @NotNull(message = "Transaction is required")
    @Schema(description = "Associated transaction")
    private Transaction transaction;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Rule type is required")
    @Size(max = 50, message = "Rule type must not exceed 50 characters")
    @Schema(description = "Type of fraud rule triggered", example = "VELOCITY_CHECK")
    private String ruleType;
    
    @Column(nullable = false, length = 500)
    @NotBlank(message = "Rule description is required")
    @Size(max = 500, message = "Rule description must not exceed 500 characters")
    @Schema(description = "Description of the fraud rule", example = "Transaction velocity exceeded threshold")
    private String ruleDescription;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Severity is required")
    @Schema(description = "Severity level of the fraud alert", example = "HIGH")
    private FraudSeverity severity;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Current status of the fraud alert", example = "ACTIVE")
    private FraudAlertStatus status = FraudAlertStatus.ACTIVE;
    
    @Column(nullable = false, precision = 5, scale = 2)
    @NotNull(message = "Risk score is required")
    @DecimalMin(value = "0.0", message = "Risk score must be non-negative")
    @DecimalMax(value = "100.0", message = "Risk score must not exceed 100")
    @Schema(description = "Risk score (0-100)", example = "85.5")
    private BigDecimal riskScore;
    
    @Column(nullable = false, precision = 5, scale = 2)
    @NotNull(message = "Confidence score is required")
    @DecimalMin(value = "0.0", message = "Confidence score must be non-negative")
    @DecimalMax(value = "100.0", message = "Confidence score must not exceed 100")
    @Schema(description = "Confidence score (0-100)", example = "92.3")
    private BigDecimal confidenceScore;
    
    @Column(length = 1000)
    @Size(max = 1000, message = "Alert details must not exceed 1000 characters")
    @Schema(description = "Detailed information about the alert", example = "5 transactions in 2 minutes from different locations")
    private String alertDetails;
    
    @Column(length = 100)
    @Size(max = 100, message = "Triggered by field must not exceed 100 characters")
    @Schema(description = "Field or condition that triggered the alert", example = "transaction_velocity")
    private String triggeredBy;
    
    @Column(length = 500)
    @Size(max = 500, message = "Threshold info must not exceed 500 characters")
    @Schema(description = "Information about the threshold that was exceeded", example = "Max 3 transactions per hour, actual: 5")
    private String thresholdInfo;
    
    @Column(length = 200)
    @Size(max = 200, message = "Location info must not exceed 200 characters")
    @Schema(description = "Location-related information", example = "Transaction from suspicious IP range")
    private String locationInfo;
    
    @Column(length = 200)
    @Size(max = 200, message = "Device info must not exceed 200 characters")
    @Schema(description = "Device-related information", example = "New device detected")
    private String deviceInfo;
    
    @Column(length = 200)
    @Size(max = 200, message = "Behavioral info must not exceed 200 characters")
    @Schema(description = "Behavioral pattern information", example = "Unusual spending pattern detected")
    private String behavioralInfo;
    
    @Column(length = 50)
    @Size(max = 50, message = "Assigned to field must not exceed 50 characters")
    @Schema(description = "User assigned to review this alert", example = "fraud_analyst_1")
    private String assignedTo;
    
    @Column(length = 1000)
    @Size(max = 1000, message = "Resolution notes must not exceed 1000 characters")
    @Schema(description = "Notes about how the alert was resolved", example = "Verified with customer via phone")
    private String resolutionNotes;
    
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the alert was resolved", example = "2025-07-18 14:30:00")
    private LocalDateTime resolvedAt;
    
    @Column(length = 50)
    @Size(max = 50, message = "Resolved by field must not exceed 50 characters")
    @Schema(description = "User who resolved the alert", example = "fraud_analyst_1")
    private String resolvedBy;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the alert was created", example = "2025-07-18 10:30:00")
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When the alert was last updated", example = "2025-07-18 11:00:00")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "fraudAlert", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Schema(description = "Audit logs associated with this fraud alert")
    private List<AuditLog> auditLogs;
    
    // Custom setter for status to update timestamp
    public void setStatus(FraudAlertStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == FraudAlertStatus.RESOLVED || status == FraudAlertStatus.DISMISSED) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
    
    // JPA Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}