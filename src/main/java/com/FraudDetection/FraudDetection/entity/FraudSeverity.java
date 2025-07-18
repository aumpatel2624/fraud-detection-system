package com.FraudDetection.FraudDetection.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Severity level of a fraud alert")
public enum FraudSeverity {
    
    @Schema(description = "Low severity - minimal risk")
    LOW,
    
    @Schema(description = "Medium severity - moderate risk")
    MEDIUM,
    
    @Schema(description = "High severity - significant risk")
    HIGH,
    
    @Schema(description = "Critical severity - immediate attention required")
    CRITICAL
}