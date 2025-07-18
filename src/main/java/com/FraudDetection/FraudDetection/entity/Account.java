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

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_number", columnList = "accountNumber"),
    @Index(name = "idx_account_customer_id", columnList = "customer_id"),
    @Index(name = "idx_account_status", columnList = "status"),
    @Index(name = "idx_account_type", columnList = "accountType"),
    @Index(name = "idx_account_created_at", columnList = "createdAt"),
    @Index(name = "idx_account_risk_level", columnList = "riskLevel")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"customer"})
@Schema(description = "Account entity representing a bank account")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique account ID", example = "1")
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "Account number is required")
    @Size(max = 20, message = "Account number must not exceed 20 characters")
    @Schema(description = "Unique account number", example = "ACC-001234")
    private String accountNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer is required")
    @Schema(description = "Account owner")
    private Customer customer;
    
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Account type is required")
    @Schema(description = "Type of account", example = "CHECKING")
    private AccountType accountType;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Account status", example = "ACTIVE")
    private AccountStatus status = AccountStatus.ACTIVE;
    
    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", message = "Balance must be non-negative")
    @Schema(description = "Current account balance", example = "5000.00")
    private BigDecimal balance;
    
    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Available balance is required")
    @DecimalMin(value = "0.0", message = "Available balance must be non-negative")
    @Schema(description = "Available balance (after holds)", example = "4800.00")
    private BigDecimal availableBalance;
    
    @Column(precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "Credit limit must be non-negative")
    @Schema(description = "Credit limit for the account", example = "10000.00")
    private BigDecimal creditLimit;
    
    @Column(precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "Daily limit must be non-negative")
    @Schema(description = "Daily transaction limit", example = "2000.00")
    private BigDecimal dailyLimit;
    
    @Column(precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "Monthly limit must be non-negative")
    @Schema(description = "Monthly transaction limit", example = "50000.00")
    private BigDecimal monthlyLimit;
    
    @Column(nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    @Schema(description = "Account currency", example = "USD")
    private String currency;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Account risk level", example = "LOW")
    private RiskLevel riskLevel = RiskLevel.LOW;
    
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last transaction timestamp", example = "2025-07-18 09:45:00")
    private LocalDateTime lastTransactionDate;
    
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last login timestamp", example = "2025-07-18 10:30:00")
    private LocalDateTime lastAccessDate;
    
    @Column(length = 50)
    @Size(max = 50, message = "Branch code must not exceed 50 characters")
    @Schema(description = "Branch code where account was opened", example = "BR-NYC-001")
    private String branchCode;
    
    @Column(length = 100)
    @Size(max = 100, message = "Account manager must not exceed 100 characters")
    @Schema(description = "Account manager name", example = "Jane Smith")
    private String accountManager;
    
    @Column
    @Schema(description = "Whether account is flagged for monitoring", example = "false")
    private Boolean flaggedForMonitoring = false;
    
    @Column(length = 500)
    @Size(max = 500, message = "Monitoring reason must not exceed 500 characters")
    @Schema(description = "Reason for monitoring flag", example = "Suspicious transaction patterns detected")
    private String monitoringReason;
    
    @Column(length = 500)
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(description = "Additional notes about the account", example = "High-value customer account")
    private String notes;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When account was opened", example = "2025-01-15 09:30:00")
    private LocalDateTime openedAt;
    
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When account was closed", example = "2025-12-31 17:00:00")
    private LocalDateTime closedAt;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
    
    // JPA Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.openedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.availableBalance == null) {
            this.availableBalance = this.balance;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}