package com.FraudDetection.FraudDetection.service;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResult {
    
    private String ruleName;
    private String ruleVersion;
    private boolean triggered;
    private BigDecimal score;
    private String reason;
    private String severity;
    private LocalDateTime evaluatedAt;
    private long executionTimeMs;
    private Map<String, Object> ruleData;
    private String recommendation;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public boolean isHighSeverity() {
        return "HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity);
    }
    
    public boolean isCriticalSeverity() {
        return "CRITICAL".equalsIgnoreCase(severity);
    }
    
    public boolean hasHighScore() {
        return score != null && score.compareTo(BigDecimal.valueOf(70)) >= 0;
    }
    
    public static RuleResult triggered(String ruleName, BigDecimal score, String reason) {
        return RuleResult.builder()
            .ruleName(ruleName)
            .triggered(true)
            .score(score)
            .reason(reason)
            .evaluatedAt(LocalDateTime.now())
            .severity(determineSeverity(score))
            .build();
    }
    
    public static RuleResult notTriggered(String ruleName) {
        return RuleResult.builder()
            .ruleName(ruleName)
            .triggered(false)
            .score(BigDecimal.ZERO)
            .reason("Rule conditions not met")
            .evaluatedAt(LocalDateTime.now())
            .severity("LOW")
            .build();
    }
    
    public static RuleResult error(String ruleName, String errorMessage) {
        return RuleResult.builder()
            .ruleName(ruleName)
            .triggered(false)
            .score(BigDecimal.ZERO)
            .reason("Rule execution error: " + errorMessage)
            .evaluatedAt(LocalDateTime.now())
            .severity("ERROR")
            .build();
    }
    
    private static String determineSeverity(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "CRITICAL";
        } else if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "HIGH";
        } else if (score.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    public String getSummary() {
        return String.format("%s: %s (Score: %.2f, Severity: %s)", 
            ruleName, 
            triggered ? "TRIGGERED" : "NOT_TRIGGERED", 
            score != null ? score : BigDecimal.ZERO, 
            severity != null ? severity : "UNKNOWN");
    }
}