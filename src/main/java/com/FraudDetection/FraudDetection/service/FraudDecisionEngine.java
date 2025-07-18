package com.FraudDetection.FraudDetection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDecisionEngine {
    
    @Value("${fraud.decision.auto-approve-threshold:30}")
    private int autoApproveThreshold;
    
    @Value("${fraud.decision.manual-review-threshold:70}")
    private int manualReviewThreshold;
    
    @Value("${fraud.decision.auto-reject-threshold:85}")
    private int autoRejectThreshold;
    
    @Value("${fraud.decision.high-confidence-threshold:80}")
    private int highConfidenceThreshold;
    
    @Value("${fraud.decision.critical-rules:VELOCITY_RULE,GEO_LOCATION_RULE}")
    private List<String> criticalRules;
    
    public FraudDecision makeDecision(FraudDetectionResult result) {
        log.debug("Making fraud decision for transaction: {}", result.getTransactionId());
        
        BigDecimal riskScore = result.getRiskScore();
        List<RuleResult> triggeredRules = result.getTriggeredRuleResults();
        
        // Check for critical rule violations
        boolean hasCriticalRuleViolation = hasCriticalRuleViolation(triggeredRules);
        
        // Check for multiple high-severity rules
        boolean hasMultipleHighSeverityRules = hasMultipleHighSeverityRules(triggeredRules);
        
        // Make decision based on risk score and rule characteristics
        FraudDecision decision = determineDecision(riskScore, hasCriticalRuleViolation, 
            hasMultipleHighSeverityRules, triggeredRules);
        
        // Calculate confidence level
        BigDecimal confidence = calculateConfidenceLevel(riskScore, triggeredRules);
        decision.setConfidenceLevel(confidence);
        
        log.info("Fraud decision made for transaction {}: {} (confidence: {}%, risk score: {})", 
            result.getTransactionId(), decision.getDecision(), confidence, riskScore);
        
        return decision;
    }
    
    private FraudDecision determineDecision(BigDecimal riskScore, boolean hasCriticalRuleViolation, 
                                          boolean hasMultipleHighSeverityRules, List<RuleResult> triggeredRules) {
        
        List<String> contributingFactors = buildContributingFactors(riskScore, hasCriticalRuleViolation, 
            hasMultipleHighSeverityRules, triggeredRules);
        
        // Auto-reject scenarios
        if (shouldAutoReject(riskScore, hasCriticalRuleViolation, hasMultipleHighSeverityRules)) {
            String reason = buildRejectReason(riskScore, hasCriticalRuleViolation, hasMultipleHighSeverityRules);
            return FraudDecision.rejected(reason, contributingFactors);
        }
        
        // Manual review scenarios
        if (shouldRequireManualReview(riskScore, hasCriticalRuleViolation, triggeredRules)) {
            String reason = buildReviewReason(riskScore, hasCriticalRuleViolation, triggeredRules);
            boolean requiresEscalation = shouldEscalate(riskScore, hasCriticalRuleViolation, hasMultipleHighSeverityRules);
            return FraudDecision.requiresReview(reason, contributingFactors, requiresEscalation);
        }
        
        // Auto-approve scenario
        String reason = "Risk score below threshold and no critical fraud indicators detected";
        return FraudDecision.approved(reason);
    }
    
    private boolean shouldAutoReject(BigDecimal riskScore, boolean hasCriticalRuleViolation, 
                                   boolean hasMultipleHighSeverityRules) {
        
        // Auto-reject if risk score is very high
        if (riskScore.compareTo(BigDecimal.valueOf(autoRejectThreshold)) >= 0) {
            return true;
        }
        
        // Auto-reject if critical rule violated with high risk score
        if (hasCriticalRuleViolation && riskScore.compareTo(BigDecimal.valueOf(manualReviewThreshold)) >= 0) {
            return true;
        }
        
        // Auto-reject if multiple high-severity rules triggered
        if (hasMultipleHighSeverityRules && riskScore.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return true;
        }
        
        return false;
    }
    
    private boolean shouldRequireManualReview(BigDecimal riskScore, boolean hasCriticalRuleViolation, 
                                            List<RuleResult> triggeredRules) {
        
        // Manual review if risk score is in the review range
        if (riskScore.compareTo(BigDecimal.valueOf(manualReviewThreshold)) >= 0) {
            return true;
        }
        
        // Manual review if critical rule is violated
        if (hasCriticalRuleViolation) {
            return true;
        }
        
        // Manual review if moderate risk score with multiple triggered rules
        if (riskScore.compareTo(BigDecimal.valueOf(50)) >= 0 && triggeredRules.size() >= 2) {
            return true;
        }
        
        // Manual review if any high-severity rule is triggered
        if (triggeredRules.stream().anyMatch(rule -> "HIGH".equals(rule.getSeverity()) || "CRITICAL".equals(rule.getSeverity()))) {
            return true;
        }
        
        return false;
    }
    
    private boolean shouldEscalate(BigDecimal riskScore, boolean hasCriticalRuleViolation, 
                                 boolean hasMultipleHighSeverityRules) {
        
        // Escalate if risk score is very high but not auto-rejected
        if (riskScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return true;
        }
        
        // Escalate if critical rule violated
        if (hasCriticalRuleViolation) {
            return true;
        }
        
        // Escalate if multiple high-severity rules
        if (hasMultipleHighSeverityRules) {
            return true;
        }
        
        return false;
    }
    
    private boolean hasCriticalRuleViolation(List<RuleResult> triggeredRules) {
        return triggeredRules.stream()
            .anyMatch(rule -> criticalRules.contains(rule.getRuleName()) && rule.isTriggered());
    }
    
    private boolean hasMultipleHighSeverityRules(List<RuleResult> triggeredRules) {
        long highSeverityCount = triggeredRules.stream()
            .filter(rule -> "HIGH".equals(rule.getSeverity()) || "CRITICAL".equals(rule.getSeverity()))
            .count();
        
        return highSeverityCount >= 2;
    }
    
    private BigDecimal calculateConfidenceLevel(BigDecimal riskScore, List<RuleResult> triggeredRules) {
        BigDecimal baseConfidence = BigDecimal.valueOf(50); // Start with 50% confidence
        
        // Increase confidence based on risk score
        if (riskScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            baseConfidence = baseConfidence.add(BigDecimal.valueOf(30));
        } else if (riskScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            baseConfidence = baseConfidence.add(BigDecimal.valueOf(20));
        } else if (riskScore.compareTo(BigDecimal.valueOf(40)) >= 0) {
            baseConfidence = baseConfidence.add(BigDecimal.valueOf(10));
        }
        
        // Increase confidence based on number of triggered rules
        int triggeredCount = triggeredRules.size();
        if (triggeredCount >= 3) {
            baseConfidence = baseConfidence.add(BigDecimal.valueOf(15));
        } else if (triggeredCount >= 2) {
            baseConfidence = baseConfidence.add(BigDecimal.valueOf(10));
        } else if (triggeredCount == 1) {
            baseConfidence = baseConfidence.add(BigDecimal.valueOf(5));
        }
        
        // Increase confidence for critical rule violations
        if (hasCriticalRuleViolation(triggeredRules)) {
            baseConfidence = baseConfidence.add(BigDecimal.valueOf(10));
        }
        
        // Cap confidence at 95%
        return baseConfidence.min(BigDecimal.valueOf(95));
    }
    
    private String buildRejectReason(BigDecimal riskScore, boolean hasCriticalRuleViolation, 
                                   boolean hasMultipleHighSeverityRules) {
        StringBuilder reason = new StringBuilder("Transaction rejected due to: ");
        
        if (riskScore.compareTo(BigDecimal.valueOf(autoRejectThreshold)) >= 0) {
            reason.append("Very high risk score (").append(riskScore).append("); ");
        }
        
        if (hasCriticalRuleViolation) {
            reason.append("Critical fraud rule violation; ");
        }
        
        if (hasMultipleHighSeverityRules) {
            reason.append("Multiple high-severity fraud indicators; ");
        }
        
        return reason.toString().trim();
    }
    
    private String buildReviewReason(BigDecimal riskScore, boolean hasCriticalRuleViolation, 
                                   List<RuleResult> triggeredRules) {
        StringBuilder reason = new StringBuilder("Manual review required due to: ");
        
        if (riskScore.compareTo(BigDecimal.valueOf(manualReviewThreshold)) >= 0) {
            reason.append("High risk score (").append(riskScore).append("); ");
        }
        
        if (hasCriticalRuleViolation) {
            reason.append("Critical fraud rule triggered; ");
        }
        
        if (triggeredRules.size() >= 2) {
            reason.append("Multiple fraud indicators (").append(triggeredRules.size()).append("); ");
        }
        
        return reason.toString().trim();
    }
    
    private List<String> buildContributingFactors(BigDecimal riskScore, boolean hasCriticalRuleViolation, 
                                                boolean hasMultipleHighSeverityRules, List<RuleResult> triggeredRules) {
        List<String> factors = new ArrayList<>();
        
        factors.add("Risk Score: " + riskScore);
        
        if (hasCriticalRuleViolation) {
            factors.add("Critical Rule Violation");
        }
        
        if (hasMultipleHighSeverityRules) {
            factors.add("Multiple High-Severity Rules");
        }
        
        for (RuleResult rule : triggeredRules) {
            factors.add(rule.getRuleName() + " (Score: " + rule.getScore() + ", Severity: " + rule.getSeverity() + ")");
        }
        
        return factors;
    }
    
    public DecisionMetrics getDecisionMetrics(BigDecimal riskScore, List<RuleResult> triggeredRules) {
        boolean hasCriticalRuleViolation = hasCriticalRuleViolation(triggeredRules);
        boolean hasMultipleHighSeverityRules = hasMultipleHighSeverityRules(triggeredRules);
        BigDecimal confidence = calculateConfidenceLevel(riskScore, triggeredRules);
        
        return DecisionMetrics.builder()
            .riskScore(riskScore)
            .confidenceLevel(confidence)
            .triggeredRuleCount(triggeredRules.size())
            .hasCriticalRuleViolation(hasCriticalRuleViolation)
            .hasMultipleHighSeverityRules(hasMultipleHighSeverityRules)
            .autoApproveThreshold(BigDecimal.valueOf(autoApproveThreshold))
            .manualReviewThreshold(BigDecimal.valueOf(manualReviewThreshold))
            .autoRejectThreshold(BigDecimal.valueOf(autoRejectThreshold))
            .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class DecisionMetrics {
        private BigDecimal riskScore;
        private BigDecimal confidenceLevel;
        private int triggeredRuleCount;
        private boolean hasCriticalRuleViolation;
        private boolean hasMultipleHighSeverityRules;
        private BigDecimal autoApproveThreshold;
        private BigDecimal manualReviewThreshold;
        private BigDecimal autoRejectThreshold;
    }
}