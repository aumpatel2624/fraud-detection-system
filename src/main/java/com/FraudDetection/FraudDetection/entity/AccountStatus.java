package com.FraudDetection.FraudDetection.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of a bank account")
public enum AccountStatus {
    
    @Schema(description = "Account is active and operational")
    ACTIVE,
    
    @Schema(description = "Account is inactive")
    INACTIVE,
    
    @Schema(description = "Account is suspended")
    SUSPENDED,
    
    @Schema(description = "Account is closed")
    CLOSED,
    
    @Schema(description = "Account is frozen due to fraud or legal issues")
    FROZEN,
    
    @Schema(description = "Account is blocked temporarily")
    BLOCKED,
    
    @Schema(description = "Account is under review")
    UNDER_REVIEW,
    
    @Schema(description = "Account is pending approval")
    PENDING_APPROVAL,
    
    @Schema(description = "Account is dormant due to inactivity")
    DORMANT,
    
    @Schema(description = "Account is restricted with limited functionality")
    RESTRICTED
}