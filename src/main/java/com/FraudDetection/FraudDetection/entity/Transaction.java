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
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_timestamp", columnList = "timestamp"),
    @Index(name = "idx_transaction_account_id", columnList = "accountId"),
    @Index(name = "idx_transaction_merchant_id", columnList = "merchantId"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_amount", columnList = "amount"),
    @Index(name = "idx_transaction_reference", columnList = "transactionReference")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"fraudAlerts", "auditLogs"})
@Schema(description = "Transaction entity representing a financial transaction")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique transaction ID", example = "1")
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Transaction reference is required")
    @Size(max = 50, message = "Transaction reference must not exceed 50 characters")
    @Schema(description = "Unique transaction reference", example = "TXN-2025-001234")
    private String transactionReference;
    
    @Column(nullable = false, length = 20)
    @NotBlank(message = "Account ID is required")
    @Size(max = 20, message = "Account ID must not exceed 20 characters")
    @Schema(description = "Account identifier", example = "ACC-001234")
    private String accountId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Transaction amount", example = "1000.50")
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    @Schema(description = "Currency code", example = "USD")
    private String currency;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Merchant ID is required")
    @Size(max = 50, message = "Merchant ID must not exceed 50 characters")
    @Schema(description = "Merchant identifier", example = "MERCHANT-001")
    private String merchantId;
    
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Merchant name is required")
    @Size(max = 100, message = "Merchant name must not exceed 100 characters")
    @Schema(description = "Merchant name", example = "Amazon Store")
    private String merchantName;
    
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction type is required")
    @Schema(description = "Type of transaction", example = "PURCHASE")
    private TransactionType transactionType;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Current transaction status", example = "PENDING")
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Transaction timestamp", example = "2025-07-18 10:30:00")
    private LocalDateTime timestamp;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Schema(description = "Transaction location", example = "New York, NY, USA")
    private String location;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "Invalid IP address format")
    @Schema(description = "IP address of transaction", example = "192.168.1.100")
    private String ipAddress;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "User agent is required")
    @Size(max = 200, message = "User agent must not exceed 200 characters")
    @Schema(description = "User agent string", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;
    
    @Column(nullable = false, length = 20)
    @NotBlank(message = "Device ID is required")
    @Size(max = 20, message = "Device ID must not exceed 20 characters")
    @Schema(description = "Device identifier", example = "DEV-001234")
    private String deviceId;
    
    @Column(length = 4)
    @Pattern(regexp = "^[0-9]{4}$", message = "MCC must be a 4-digit number")
    @Schema(description = "Merchant Category Code", example = "5411")
    private String mcc; // Merchant Category Code
    
    @Column(length = 50)
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Schema(description = "Payment method used", example = "CREDIT_CARD")
    private String paymentMethod;
    
    @Column(precision = 10, scale = 6)
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "Latitude coordinate", example = "40.7128")
    private BigDecimal latitude;
    
    @Column(precision = 10, scale = 6)
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "Longitude coordinate", example = "-74.0060")
    private BigDecimal longitude;
    
    @Column(length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Transaction description", example = "Purchase at Amazon Store")
    private String description;
    
    @Column(length = 20)
    @Size(max = 20, message = "Card last 4 digits must not exceed 20 characters")
    @Schema(description = "Last 4 digits of card", example = "1234")
    private String cardLast4;
    
    @Column(length = 50)
    @Size(max = 50, message = "Card type must not exceed 50 characters")
    @Schema(description = "Type of card used", example = "VISA")
    private String cardType;
    
    @Column(precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "Previous balance must be non-negative")
    @Schema(description = "Account balance before transaction", example = "5000.00")
    private BigDecimal previousBalance;
    
    @Column(precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "New balance must be non-negative")
    @Schema(description = "Account balance after transaction", example = "4000.00")
    private BigDecimal newBalance;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Schema(description = "Fraud alerts associated with this transaction")
    private List<FraudAlert> fraudAlerts;
    
    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Schema(description = "Audit logs associated with this transaction")
    private List<AuditLog> auditLogs;
    
    // Custom setter for status to update timestamp
    public void setStatus(TransactionStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    // JPA Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}