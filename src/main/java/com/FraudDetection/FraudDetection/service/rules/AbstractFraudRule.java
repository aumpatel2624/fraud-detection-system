package com.FraudDetection.FraudDetection.service.rules;

import com.FraudDetection.FraudDetection.entity.Transaction;
import com.FraudDetection.FraudDetection.service.RuleResult;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
public abstract class AbstractFraudRule implements FraudRule {
    
    protected final String ruleName;
    protected final String ruleVersion;
    protected final String description;
    protected final boolean enabled;
    protected final int priority;
    
    protected AbstractFraudRule(String ruleName, String ruleVersion, String description, boolean enabled, int priority) {
        this.ruleName = ruleName;
        this.ruleVersion = ruleVersion;
        this.description = description;
        this.enabled = enabled;
        this.priority = priority;
    }
    
    @Override
    public final RuleResult evaluate(Transaction transaction) {
        if (!enabled) {
            log.debug("Rule {} is disabled, skipping evaluation", ruleName);
            return RuleResult.builder()
                .ruleName(ruleName)
                .ruleVersion(ruleVersion)
                .triggered(false)
                .score(BigDecimal.ZERO)
                .reason("Rule is disabled")
                .severity("INFO")
                .evaluatedAt(LocalDateTime.now())
                .build();
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Evaluating rule {} for transaction {}", ruleName, transaction.getTransactionReference());
            
            RuleResult result = executeRule(transaction);
            
            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTime);
            result.setRuleVersion(ruleVersion);
            result.setEvaluatedAt(LocalDateTime.now());
            
            log.debug("Rule {} evaluation completed for transaction {} in {}ms: triggered={}, score={}", 
                ruleName, transaction.getTransactionReference(), executionTime, result.isTriggered(), result.getScore());
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Error evaluating rule {} for transaction {}: {}", ruleName, transaction.getTransactionReference(), e.getMessage(), e);
            
            return RuleResult.builder()
                .ruleName(ruleName)
                .ruleVersion(ruleVersion)
                .triggered(false)
                .score(BigDecimal.ZERO)
                .reason("Rule execution failed: " + e.getMessage())
                .severity("ERROR")
                .evaluatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .build();
        }
    }
    
    /**
     * Subclasses implement this method to provide rule-specific evaluation logic
     * @param transaction The transaction to evaluate
     * @return RuleResult containing evaluation outcome
     */
    protected abstract RuleResult executeRule(Transaction transaction);
    
    @Override
    public String getRuleName() {
        return ruleName;
    }
    
    @Override
    public String getRuleVersion() {
        return ruleVersion;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    /**
     * Helper method to create a triggered rule result
     */
    protected RuleResult createTriggeredResult(BigDecimal score, String reason) {
        return RuleResult.builder()
            .ruleName(ruleName)
            .triggered(true)
            .score(score)
            .reason(reason)
            .severity(determineSeverity(score))
            .build();
    }
    
    /**
     * Helper method to create a non-triggered rule result
     */
    protected RuleResult createNotTriggeredResult() {
        return RuleResult.builder()
            .ruleName(ruleName)
            .triggered(false)
            .score(BigDecimal.ZERO)
            .reason("Rule conditions not met")
            .severity("LOW")
            .build();
    }
    
    /**
     * Helper method to determine severity based on score
     */
    protected String determineSeverity(BigDecimal score) {
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
}