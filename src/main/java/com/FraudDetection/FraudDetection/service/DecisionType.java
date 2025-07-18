package com.FraudDetection.FraudDetection.service;

public enum DecisionType {
    APPROVED("Transaction approved for processing"),
    REJECTED("Transaction rejected due to fraud indicators"),
    REQUIRES_REVIEW("Transaction requires manual review");
    
    private final String description;
    
    DecisionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}