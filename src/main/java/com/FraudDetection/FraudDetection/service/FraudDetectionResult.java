package com.FraudDetection.FraudDetection.service;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionResult {
    
    private String transactionId;
    private LocalDateTime processedAt;
    private BigDecimal riskScore;
    private BigDecimal confidenceScore;
    private FraudDecision fraudDecision;
    
    @Builder.Default
    private List<RuleResult> ruleResults = new ArrayList<>();
    
    public void addRuleResult(RuleResult ruleResult) {
        if (this.ruleResults == null) {
            this.ruleResults = new ArrayList<>();
        }
        this.ruleResults.add(ruleResult);
    }
    
    public boolean isFraudulent() {
        return fraudDecision != null && fraudDecision.getDecision() == DecisionType.REJECTED;
    }
    
    public boolean requiresReview() {
        return fraudDecision != null && fraudDecision.getDecision() == DecisionType.REQUIRES_REVIEW;
    }
    
    public boolean isApproved() {
        return fraudDecision != null && fraudDecision.getDecision() == DecisionType.APPROVED;
    }
    
    public List<RuleResult> getTriggeredRuleResults() {
        return ruleResults.stream()
            .filter(RuleResult::isTriggered)
            .collect(Collectors.toList());
    }
    
    public String getTriggeredRules() {
        return getTriggeredRuleResults().stream()
            .map(RuleResult::getRuleName)
            .collect(Collectors.joining(", "));
    }
    
    public int getTriggeredRuleCount() {
        return getTriggeredRuleResults().size();
    }
    
    public BigDecimal getTotalRuleScore() {
        return ruleResults.stream()
            .map(RuleResult::getScore)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getMaxRuleScore() {
        return ruleResults.stream()
            .map(RuleResult::getScore)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }
    
    public String getDescription() {
        if (getTriggeredRuleCount() == 0) {
            return "No fraud indicators detected";
        }
        
        StringBuilder description = new StringBuilder();
        description.append(String.format("Detected %d fraud indicators: ", getTriggeredRuleCount()));
        
        List<String> reasons = getTriggeredRuleResults().stream()
            .map(RuleResult::getReason)
            .filter(reason -> reason != null && !reason.trim().isEmpty())
            .collect(Collectors.toList());
            
        description.append(String.join("; ", reasons));
        
        return description.toString();
    }
    
    public String getRecommendedAction() {
        if (isFraudulent()) {
            return "REJECT_TRANSACTION";
        } else if (requiresReview()) {
            return "MANUAL_REVIEW";
        } else if (getTriggeredRuleCount() > 0) {
            return "ENHANCED_MONITORING";
        } else {
            return "APPROVE";
        }
    }
    
    public boolean hasHighRiskScore() {
        return riskScore != null && riskScore.compareTo(BigDecimal.valueOf(70)) >= 0;
    }
    
    public boolean hasCriticalRiskScore() {
        return riskScore != null && riskScore.compareTo(BigDecimal.valueOf(90)) >= 0;
    }
    
    public String getSummary() {
        return String.format(
            "Transaction %s processed at %s: Decision=%s, Risk Score=%.2f, Triggered Rules=%d",
            transactionId,
            processedAt,
            fraudDecision != null ? fraudDecision.getDecision() : "UNKNOWN",
            riskScore != null ? riskScore : BigDecimal.ZERO,
            getTriggeredRuleCount()
        );
    }
}