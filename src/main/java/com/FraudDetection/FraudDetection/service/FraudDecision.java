package com.FraudDetection.FraudDetection.service;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDecision {
    
    private DecisionType decision;
    private String reason;
    private BigDecimal confidenceLevel;
    private LocalDateTime decidedAt;
    private String decidedBy;
    private List<String> contributingFactors;
    private String recommendedAction;
    private boolean requiresEscalation;
    private String escalationReason;
    
    public boolean isApproved() {
        return decision == DecisionType.APPROVED;
    }
    
    public boolean isRejected() {
        return decision == DecisionType.REJECTED;
    }
    
    public boolean requiresReview() {
        return decision == DecisionType.REQUIRES_REVIEW;
    }
    
    public boolean hasHighConfidence() {
        return confidenceLevel != null && confidenceLevel.compareTo(BigDecimal.valueOf(80)) >= 0;
    }
    
    public boolean hasLowConfidence() {
        return confidenceLevel != null && confidenceLevel.compareTo(BigDecimal.valueOf(50)) < 0;
    }
    
    public static FraudDecision approved(String reason) {
        return FraudDecision.builder()
            .decision(DecisionType.APPROVED)
            .reason(reason)
            .decidedAt(LocalDateTime.now())
            .decidedBy("SYSTEM")
            .confidenceLevel(BigDecimal.valueOf(95))
            .recommendedAction("PROCESS_TRANSACTION")
            .requiresEscalation(false)
            .build();
    }
    
    public static FraudDecision rejected(String reason, List<String> contributingFactors) {
        return FraudDecision.builder()
            .decision(DecisionType.REJECTED)
            .reason(reason)
            .decidedAt(LocalDateTime.now())
            .decidedBy("SYSTEM")
            .contributingFactors(contributingFactors)
            .confidenceLevel(BigDecimal.valueOf(90))
            .recommendedAction("BLOCK_TRANSACTION")
            .requiresEscalation(false)
            .build();
    }
    
    public static FraudDecision requiresReview(String reason, List<String> contributingFactors, boolean escalate) {
        return FraudDecision.builder()
            .decision(DecisionType.REQUIRES_REVIEW)
            .reason(reason)
            .decidedAt(LocalDateTime.now())
            .decidedBy("SYSTEM")
            .contributingFactors(contributingFactors)
            .confidenceLevel(BigDecimal.valueOf(60))
            .recommendedAction("MANUAL_REVIEW")
            .requiresEscalation(escalate)
            .escalationReason(escalate ? "High risk factors detected requiring senior review" : null)
            .build();
    }
}