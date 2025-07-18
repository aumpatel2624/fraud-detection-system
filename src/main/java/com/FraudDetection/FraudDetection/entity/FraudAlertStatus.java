package com.FraudDetection.FraudDetection.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of a fraud alert")
public enum FraudAlertStatus {
    
    @Schema(description = "Alert is active and needs attention")
    ACTIVE,
    
    @Schema(description = "Alert is under investigation")
    UNDER_INVESTIGATION,
    
    @Schema(description = "Alert has been resolved")
    RESOLVED,
    
    @Schema(description = "Alert has been dismissed as false positive")
    DISMISSED,
    
    @Schema(description = "Alert is escalated to higher authority")
    ESCALATED,
    
    @Schema(description = "Alert is on hold pending additional information")
    ON_HOLD,
    
    @Schema(description = "Alert is assigned to an analyst")
    ASSIGNED,
    
    @Schema(description = "Alert is closed")
    CLOSED
}