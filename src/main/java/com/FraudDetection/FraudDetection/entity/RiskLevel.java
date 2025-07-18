package com.FraudDetection.FraudDetection.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Risk level assessment for customers and accounts")
public enum RiskLevel {
    
    @Schema(description = "Low risk - minimal monitoring required")
    LOW,
    
    @Schema(description = "Medium risk - standard monitoring")
    MEDIUM,
    
    @Schema(description = "High risk - enhanced monitoring required")
    HIGH,
    
    @Schema(description = "Very high risk - strict monitoring and controls")
    VERY_HIGH,
    
    @Schema(description = "Unknown risk - assessment pending")
    UNKNOWN
}