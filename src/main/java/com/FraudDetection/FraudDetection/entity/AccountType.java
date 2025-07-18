package com.FraudDetection.FraudDetection.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of bank account")
public enum AccountType {
    
    @Schema(description = "Checking account for daily transactions")
    CHECKING,
    
    @Schema(description = "Savings account for storing money")
    SAVINGS,
    
    @Schema(description = "Credit card account")
    CREDIT_CARD,
    
    @Schema(description = "Business checking account")
    BUSINESS_CHECKING,
    
    @Schema(description = "Business savings account")
    BUSINESS_SAVINGS,
    
    @Schema(description = "Money market account")
    MONEY_MARKET,
    
    @Schema(description = "Certificate of deposit")
    CD,
    
    @Schema(description = "Individual retirement account")
    IRA,
    
    @Schema(description = "Loan account")
    LOAN,
    
    @Schema(description = "Mortgage account")
    MORTGAGE,
    
    @Schema(description = "Investment account")
    INVESTMENT,
    
    @Schema(description = "Trust account")
    TRUST,
    
    @Schema(description = "Joint account")
    JOINT,
    
    @Schema(description = "Student account")
    STUDENT,
    
    @Schema(description = "Senior account")
    SENIOR
}