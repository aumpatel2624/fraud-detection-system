package com.FraudDetection.FraudDetection.service.rules;

import com.FraudDetection.FraudDetection.entity.Transaction;
import com.FraudDetection.FraudDetection.service.RuleResult;

public interface FraudRule {
    
    /**
     * Evaluates the fraud rule against a transaction
     * @param transaction The transaction to evaluate
     * @return RuleResult containing evaluation outcome
     */
    RuleResult evaluate(Transaction transaction);
    
    /**
     * Gets the name of the fraud rule
     * @return The rule name
     */
    String getRuleName();
    
    /**
     * Gets the version of the fraud rule
     * @return The rule version
     */
    String getRuleVersion();
    
    /**
     * Gets the description of what this rule detects
     * @return The rule description
     */
    String getDescription();
    
    /**
     * Indicates if this rule is enabled
     * @return true if rule is enabled, false otherwise
     */
    boolean isEnabled();
    
    /**
     * Gets the priority of this rule (higher number = higher priority)
     * @return The rule priority
     */
    int getPriority();
}