package com.FraudDetection.FraudDetection.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Severity level of an audit event")
public enum AuditSeverity {
    
    @Schema(description = "Informational audit event")
    INFO,
    
    @Schema(description = "Warning audit event")
    WARNING,
    
    @Schema(description = "Error audit event")
    ERROR,
    
    @Schema(description = "Critical audit event")
    CRITICAL,
    
    @Schema(description = "Debug audit event")
    DEBUG
}