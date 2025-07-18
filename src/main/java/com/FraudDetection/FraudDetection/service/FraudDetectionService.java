package com.FraudDetection.FraudDetection.service;

import com.FraudDetection.FraudDetection.entity.*;
import com.FraudDetection.FraudDetection.repository.FraudAlertRepository;
import com.FraudDetection.FraudDetection.repository.TransactionRepository;
import com.FraudDetection.FraudDetection.repository.AuditLogRepository;
import com.FraudDetection.FraudDetection.service.rules.FraudRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final AuditLogRepository auditLogRepository;
    private final List<FraudRule> fraudRules;
    private final RiskScoringService riskScoringService;
    private final FraudDecisionEngine fraudDecisionEngine;

    @Transactional
    public FraudDetectionResult processTransaction(Transaction transaction) {
        log.info("Processing transaction for fraud detection: {}", transaction.getTransactionReference());
        
        try {
            // Save transaction first
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            // Create audit log entry
            createAuditLogEntry(savedTransaction, "FRAUD_DETECTION_STARTED", "Starting fraud detection process");
            
            // Execute fraud detection rules
            FraudDetectionResult result = executeRulesEngine(savedTransaction);
            
            // Calculate risk score
            BigDecimal riskScore = riskScoringService.calculateRiskScore(savedTransaction, result);
            result.setRiskScore(riskScore);
            
            // Make fraud decision
            FraudDecision decision = fraudDecisionEngine.makeDecision(result);
            result.setFraudDecision(decision);
            
            // Create fraud alert if needed
            if (result.isFraudulent() || result.requiresReview()) {
                createFraudAlert(savedTransaction, result);
            }
            
            // Update transaction status
            updateTransactionStatus(savedTransaction, result);
            
            // Create final audit log entry
            createAuditLogEntry(savedTransaction, "FRAUD_DETECTION_COMPLETED", 
                String.format("Fraud detection completed. Decision: %s, Risk Score: %s", 
                    decision.getDecision(), riskScore));
            
            log.info("Fraud detection completed for transaction: {} with decision: {}", 
                savedTransaction.getTransactionReference(), decision.getDecision());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error processing transaction for fraud detection: {}", transaction.getTransactionReference(), e);
            createAuditLogEntry(transaction, "FRAUD_DETECTION_ERROR", "Error during fraud detection: " + e.getMessage());
            throw new FraudDetectionException("Failed to process transaction for fraud detection", e);
        }
    }

    private FraudDetectionResult executeRulesEngine(Transaction transaction) {
        log.debug("Executing fraud detection rules for transaction: {}", transaction.getTransactionReference());
        
        FraudDetectionResult result = new FraudDetectionResult();
        result.setTransactionId(transaction.getTransactionReference());
        result.setProcessedAt(LocalDateTime.now());
        
        // Execute all fraud rules
        for (FraudRule rule : fraudRules) {
            try {
                RuleResult ruleResult = rule.evaluate(transaction);
                result.addRuleResult(ruleResult);
                
                log.debug("Rule {} executed for transaction {}: triggered={}, score={}", 
                    rule.getRuleName(), transaction.getTransactionReference(), 
                    ruleResult.isTriggered(), ruleResult.getScore());
                    
            } catch (Exception e) {
                log.error("Error executing rule {} for transaction {}: {}", 
                    rule.getRuleName(), transaction.getTransactionReference(), e.getMessage());
                
                // Create error rule result
                RuleResult errorResult = RuleResult.builder()
                    .ruleName(rule.getRuleName())
                    .triggered(false)
                    .score(BigDecimal.ZERO)
                    .reason("Rule execution failed: " + e.getMessage())
                    .build();
                result.addRuleResult(errorResult);
            }
        }
        
        return result;
    }

    private void createFraudAlert(Transaction transaction, FraudDetectionResult result) {
        log.info("Creating fraud alert for transaction: {}", transaction.getTransactionReference());
        
        FraudAlert alert = FraudAlert.builder()
            .transaction(transaction)
            .ruleType(result.getTriggeredRules())
            .ruleDescription(result.getDescription())
            .riskScore(result.getRiskScore())
            .confidenceScore(result.getConfidenceScore())
            .severity(determineSeverity(result.getRiskScore()))
            .status(FraudAlertStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();
        
        fraudAlertRepository.save(alert);
        
        createAuditLogEntry(transaction, "FRAUD_ALERT_CREATED", 
            String.format("Fraud alert created: ID %s", alert.getId()));
    }

    private FraudSeverity determineSeverity(BigDecimal riskScore) {
        if (riskScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return FraudSeverity.CRITICAL;
        } else if (riskScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return FraudSeverity.HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return FraudSeverity.MEDIUM;
        } else {
            return FraudSeverity.LOW;
        }
    }

    private void updateTransactionStatus(Transaction transaction, FraudDetectionResult result) {
        TransactionStatus newStatus;
        
        switch (result.getFraudDecision().getDecision()) {
            case APPROVED:
                newStatus = TransactionStatus.PENDING;
                break;
            case REJECTED:
                newStatus = TransactionStatus.FAILED;
                break;
            case REQUIRES_REVIEW:
                newStatus = TransactionStatus.PENDING;
                break;
            default:
                newStatus = TransactionStatus.PENDING;
        }
        
        transaction.setStatus(newStatus);
        // Note: processedAt field doesn't exist in Transaction entity
        transactionRepository.save(transaction);
        
        log.debug("Updated transaction {} status to {}", transaction.getTransactionReference(), newStatus);
    }

    private void createAuditLogEntry(Transaction transaction, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
            .transaction(transaction)
            .action(action)
            .createdAt(LocalDateTime.now())
            .build();
        
        auditLogRepository.save(auditLog);
    }

    public List<FraudAlert> getActiveAlertsForAccount(String accountId) {
        return fraudAlertRepository.findByAccountId(accountId);
    }

    public List<FraudAlert> getHighRiskAlerts() {
        return fraudAlertRepository.findHighRiskAlerts(
            BigDecimal.valueOf(70), 
            List.of(FraudAlertStatus.ACTIVE, FraudAlertStatus.ESCALATED)
        );
    }

    @Transactional
    public void resolveAlert(Long alertId, String resolvedBy, String resolution) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
            .orElseThrow(() -> new FraudDetectionException("Fraud alert not found: " + alertId));
        
        alert.setStatus(FraudAlertStatus.RESOLVED);
        alert.setResolvedBy(resolvedBy);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionNotes(resolution);
        
        fraudAlertRepository.save(alert);
        
        createAuditLogEntry(alert.getTransaction(), "FRAUD_ALERT_RESOLVED", 
            String.format("Alert %s resolved by %s: %s", alertId, resolvedBy, resolution));
        
        log.info("Fraud alert {} resolved by {}", alertId, resolvedBy);
    }
}