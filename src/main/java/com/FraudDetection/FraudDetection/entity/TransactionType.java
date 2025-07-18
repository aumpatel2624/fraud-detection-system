package com.FraudDetection.FraudDetection.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of financial transaction")
public enum TransactionType {
    
    @Schema(description = "Purchase transaction")
    PURCHASE,
    
    @Schema(description = "ATM withdrawal")
    WITHDRAWAL,
    
    @Schema(description = "Deposit transaction")
    DEPOSIT,
    
    @Schema(description = "Transfer between accounts")
    TRANSFER,
    
    @Schema(description = "Payment transaction")
    PAYMENT,
    
    @Schema(description = "Refund transaction")
    REFUND,
    
    @Schema(description = "Authorization hold")
    AUTHORIZATION,
    
    @Schema(description = "Cashback transaction")
    CASHBACK,
    
    @Schema(description = "Bill payment")
    BILL_PAYMENT,
    
    @Schema(description = "Subscription payment")
    SUBSCRIPTION,
    
    @Schema(description = "Recurring payment")
    RECURRING_PAYMENT,
    
    @Schema(description = "International transfer")
    INTERNATIONAL_TRANSFER,
    
    @Schema(description = "Wire transfer")
    WIRE_TRANSFER,
    
    @Schema(description = "Check deposit")
    CHECK_DEPOSIT,
    
    @Schema(description = "Mobile payment")
    MOBILE_PAYMENT,
    
    @Schema(description = "Online payment")
    ONLINE_PAYMENT,
    
    @Schema(description = "ATM withdrawal")
    ATM_WITHDRAWAL,
    
    @Schema(description = "Point of sale purchase")
    POS_PURCHASE,
    
    @Schema(description = "Contactless payment")
    CONTACTLESS_PAYMENT,
    
    @Schema(description = "Cryptocurrency exchange")
    CRYPTOCURRENCY_EXCHANGE,
    
    @Schema(description = "Loan payment")
    LOAN_PAYMENT,
    
    @Schema(description = "Interest payment")
    INTEREST_PAYMENT,
    
    @Schema(description = "Fee transaction")
    FEE,
    
    @Schema(description = "Chargeback transaction")
    CHARGEBACK,
    
    @Schema(description = "Adjustment transaction")
    ADJUSTMENT
}