package com.FraudDetection.FraudDetection.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of a financial transaction")
public enum TransactionStatus {
    
    @Schema(description = "Transaction is pending processing")
    PENDING,
    
    @Schema(description = "Transaction has been approved")
    APPROVED,
    
    @Schema(description = "Transaction has been declined")
    DECLINED,
    
    @Schema(description = "Transaction is blocked due to fraud suspicion")
    BLOCKED,
    
    @Schema(description = "Transaction is under manual review")
    UNDER_REVIEW,
    
    @Schema(description = "Transaction has been flagged for fraud")
    FLAGGED,
    
    @Schema(description = "Transaction has been cancelled")
    CANCELLED,
    
    @Schema(description = "Transaction has been refunded")
    REFUNDED,
    
    @Schema(description = "Transaction has been settled")
    SETTLED,
    
    @Schema(description = "Transaction is currently processing")
    PROCESSING,
    
    @Schema(description = "Transaction has failed")
    FAILED,
    
    @Schema(description = "Transaction has expired")
    EXPIRED,
    
    @Schema(description = "Transaction is authorized but not captured")
    AUTHORIZED,
    
    @Schema(description = "Transaction has been captured")
    CAPTURED,
    
    @Schema(description = "Transaction is on hold")
    ON_HOLD,
    
    @Schema(description = "Transaction is being disputed")
    DISPUTED,
    
    @Schema(description = "Transaction has been reversed")
    REVERSED,
    
    @Schema(description = "Transaction is partially refunded")
    PARTIALLY_REFUNDED,
    
    @Schema(description = "Transaction requires additional verification")
    VERIFICATION_REQUIRED
}