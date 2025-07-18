package com.FraudDetection.FraudDetection.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of a customer account")
public enum CustomerStatus {
    
    @Schema(description = "Customer account is active")
    ACTIVE,
    
    @Schema(description = "Customer account is inactive")
    INACTIVE,
    
    @Schema(description = "Customer account is suspended")
    SUSPENDED,
    
    @Schema(description = "Customer account is closed")
    CLOSED,
    
    @Schema(description = "Customer account is pending verification")
    PENDING_VERIFICATION,
    
    @Schema(description = "Customer account is blocked due to fraud")
    BLOCKED,
    
    @Schema(description = "Customer account is under review")
    UNDER_REVIEW,
    
    @Schema(description = "Customer account is frozen")
    FROZEN
}