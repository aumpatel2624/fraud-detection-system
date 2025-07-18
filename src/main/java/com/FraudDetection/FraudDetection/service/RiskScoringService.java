package com.FraudDetection.FraudDetection.service;

import com.FraudDetection.FraudDetection.entity.*;
import com.FraudDetection.FraudDetection.repository.CustomerRepository;
import com.FraudDetection.FraudDetection.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskScoringService {
    
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    
    @Value("${fraud.scoring.base-score:20}")
    private int baseScore;
    
    @Value("${fraud.scoring.max-score:100}")
    private int maxScore;
    
    @Value("${fraud.scoring.rule-weight:0.6}")
    private double ruleWeight;
    
    @Value("${fraud.scoring.transaction-weight:0.2}")
    private double transactionWeight;
    
    @Value("${fraud.scoring.account-weight:0.1}")
    private double accountWeight;
    
    @Value("${fraud.scoring.customer-weight:0.1}")
    private double customerWeight;
    
    public BigDecimal calculateRiskScore(Transaction transaction, FraudDetectionResult result) {
        log.debug("Calculating risk score for transaction: {}", transaction.getTransactionReference());
        
        try {
            // Get rule-based score
            BigDecimal ruleScore = calculateRuleBasedScore(result);
            
            // Get transaction-based score
            BigDecimal transactionScore = calculateTransactionRiskScore(transaction);
            
            // Get account-based score
            BigDecimal accountScore = calculateAccountRiskScore(transaction.getAccountId());
            
            // Get customer-based score
            BigDecimal customerScore = calculateCustomerRiskScore(transaction.getAccountId());
            
            // Calculate weighted total score
            BigDecimal totalScore = calculateWeightedScore(ruleScore, transactionScore, accountScore, customerScore);
            
            log.debug("Risk score calculated for transaction {}: rules={}, transaction={}, account={}, customer={}, total={}", 
                transaction.getTransactionReference(), ruleScore, transactionScore, accountScore, customerScore, totalScore);
            
            return totalScore;
            
        } catch (Exception e) {
            log.error("Error calculating risk score for transaction {}: {}", transaction.getTransactionReference(), e.getMessage(), e);
            return BigDecimal.valueOf(baseScore); // Return base score on error
        }
    }
    
    private BigDecimal calculateRuleBasedScore(FraudDetectionResult result) {
        if (result.getRuleResults() == null || result.getRuleResults().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate score based on triggered rules
        List<RuleResult> triggeredRules = result.getTriggeredRuleResults();
        
        if (triggeredRules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Use the maximum rule score as the base, then add factors for multiple triggered rules
        BigDecimal maxRuleScore = result.getMaxRuleScore();
        
        // Add bonus for multiple triggered rules (diminishing returns)
        int triggeredCount = triggeredRules.size();
        BigDecimal multiRuleBonus = BigDecimal.ZERO;
        
        if (triggeredCount > 1) {
            // Each additional rule adds decreasing bonus: 10%, 5%, 2%, 1%
            for (int i = 1; i < triggeredCount && i <= 4; i++) {
                double bonusPercent = Math.pow(0.5, i - 1) * 0.1; // 10%, 5%, 2.5%, 1.25%
                multiRuleBonus = multiRuleBonus.add(maxRuleScore.multiply(BigDecimal.valueOf(bonusPercent)));
            }
        }
        
        BigDecimal ruleScore = maxRuleScore.add(multiRuleBonus);
        
        // Cap at 100
        return ruleScore.min(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateTransactionRiskScore(Transaction transaction) {
        BigDecimal score = BigDecimal.ZERO;
        
        // Amount-based risk
        BigDecimal amount = transaction.getAmount();
        if (amount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            // High amount transactions are riskier
            double amountFactor = Math.min(amount.doubleValue() / 50000, 1.0); // Max factor at 50k
            score = score.add(BigDecimal.valueOf(30 * amountFactor));
        }
        
        // Time-based risk (unusual hours)
        int hour = transaction.getTimestamp().getHour();
        if (hour >= 23 || hour <= 5) {
            score = score.add(BigDecimal.valueOf(15)); // Night transactions are riskier
        }
        
        // Transaction type risk
        TransactionType type = transaction.getTransactionType();
        switch (type) {
            case CRYPTOCURRENCY_EXCHANGE:
                score = score.add(BigDecimal.valueOf(25));
                break;
            case INTERNATIONAL_TRANSFER:
            case WIRE_TRANSFER:
                score = score.add(BigDecimal.valueOf(20));
                break;
            case ONLINE_PAYMENT:
            case MOBILE_PAYMENT:
                score = score.add(BigDecimal.valueOf(10));
                break;
            case ATM_WITHDRAWAL:
                score = score.add(BigDecimal.valueOf(5));
                break;
            default:
                // Other types are considered lower risk
                break;
        }
        
        // Currency risk (foreign currency)
        if (!"USD".equals(transaction.getCurrency())) {
            score = score.add(BigDecimal.valueOf(10));
        }
        
        // Channel risk
        // Channel risk - commenting out as getChannel() method doesn't exist
        // if ("ONLINE".equalsIgnoreCase(transaction.getChannel())) {
        //     score = score.add(BigDecimal.valueOf(5));
        // }
        
        return score.min(BigDecimal.valueOf(100));
    }
    
    private BigDecimal calculateAccountRiskScore(String accountId) {
        try {
            Account account = accountRepository.findByAccountNumber(accountId).orElse(null);
            
            if (account == null) {
                return BigDecimal.valueOf(50); // Unknown account is risky
            }
            
            BigDecimal score = BigDecimal.ZERO;
            
            // Risk level-based score
            RiskLevel riskLevel = account.getRiskLevel();
            switch (riskLevel) {
                case VERY_HIGH:
                    score = score.add(BigDecimal.valueOf(40));
                    break;
                case HIGH:
                    score = score.add(BigDecimal.valueOf(30));
                    break;
                case MEDIUM:
                    score = score.add(BigDecimal.valueOf(15));
                    break;
                case LOW:
                    score = score.add(BigDecimal.valueOf(5));
                    break;
            }
            
            // Account status risk
            if (account.getStatus() == AccountStatus.SUSPENDED) {
                score = score.add(BigDecimal.valueOf(50));
            } else if (account.getStatus() == AccountStatus.RESTRICTED) {
                score = score.add(BigDecimal.valueOf(25));
            }
            
            // Flagged account risk
            if (Boolean.TRUE.equals(account.getFlaggedForMonitoring())) {
                score = score.add(BigDecimal.valueOf(20));
            }
            
            // New account risk (accounts less than 30 days old)
            if (account.getOpenedAt() != null) {
                long daysSinceOpening = ChronoUnit.DAYS.between(account.getOpenedAt(), LocalDateTime.now());
                if (daysSinceOpening < 30) {
                    score = score.add(BigDecimal.valueOf(15));
                }
            }
            
            return score.min(BigDecimal.valueOf(100));
            
        } catch (Exception e) {
            log.error("Error calculating account risk score for account {}: {}", accountId, e.getMessage());
            return BigDecimal.valueOf(25); // Default risk score on error
        }
    }
    
    private BigDecimal calculateCustomerRiskScore(String accountId) {
        try {
            Account account = accountRepository.findByAccountNumber(accountId).orElse(null);
            if (account == null) {
                return BigDecimal.valueOf(25);
            }
            
            Customer customer = account.getCustomer();
            if (customer == null) {
                return BigDecimal.valueOf(25);
            }
            
            BigDecimal score = BigDecimal.ZERO;
            
            // Customer risk level
            RiskLevel riskLevel = customer.getRiskLevel();
            switch (riskLevel) {
                case VERY_HIGH:
                    score = score.add(BigDecimal.valueOf(35));
                    break;
                case HIGH:
                    score = score.add(BigDecimal.valueOf(25));
                    break;
                case MEDIUM:
                    score = score.add(BigDecimal.valueOf(10));
                    break;
                case LOW:
                    score = score.add(BigDecimal.valueOf(3));
                    break;
            }
            
            // Customer status risk
            if (customer.getStatus() == CustomerStatus.SUSPENDED) {
                score = score.add(BigDecimal.valueOf(40));
            } else if (customer.getStatus() == CustomerStatus.BLOCKED) {
                score = score.add(BigDecimal.valueOf(35));
            } else if (customer.getStatus() == CustomerStatus.FROZEN) {
                score = score.add(BigDecimal.valueOf(20));
            }
            
            // New customer risk (customers less than 90 days)
            if (customer.getCustomerSince() != null) {
                long daysSinceJoining = ChronoUnit.DAYS.between(customer.getCustomerSince(), LocalDateTime.now());
                if (daysSinceJoining < 90) {
                    score = score.add(BigDecimal.valueOf(10));
                }
            }
            
            // Inactive customer risk (no recent login)
            if (customer.getLastLogin() != null) {
                long daysSinceLogin = ChronoUnit.DAYS.between(customer.getLastLogin(), LocalDateTime.now());
                if (daysSinceLogin > 180) {
                    score = score.add(BigDecimal.valueOf(15)); // Long inactive period
                }
            }
            
            return score.min(BigDecimal.valueOf(100));
            
        } catch (Exception e) {
            log.error("Error calculating customer risk score for account {}: {}", accountId, e.getMessage());
            return BigDecimal.valueOf(25); // Default risk score on error
        }
    }
    
    private BigDecimal calculateWeightedScore(BigDecimal ruleScore, BigDecimal transactionScore, 
                                            BigDecimal accountScore, BigDecimal customerScore) {
        
        BigDecimal weightedScore = ruleScore.multiply(BigDecimal.valueOf(ruleWeight))
            .add(transactionScore.multiply(BigDecimal.valueOf(transactionWeight)))
            .add(accountScore.multiply(BigDecimal.valueOf(accountWeight)))
            .add(customerScore.multiply(BigDecimal.valueOf(customerWeight)));
        
        // Add base score and cap at max score
        BigDecimal finalScore = weightedScore.add(BigDecimal.valueOf(baseScore));
        
        return finalScore.min(BigDecimal.valueOf(maxScore)).setScale(2, RoundingMode.HALF_UP);
    }
    
    public RiskScoreBreakdown getRiskScoreBreakdown(Transaction transaction, FraudDetectionResult result) {
        BigDecimal ruleScore = calculateRuleBasedScore(result);
        BigDecimal transactionScore = calculateTransactionRiskScore(transaction);
        BigDecimal accountScore = calculateAccountRiskScore(transaction.getAccountId());
        BigDecimal customerScore = calculateCustomerRiskScore(transaction.getAccountId());
        BigDecimal totalScore = calculateWeightedScore(ruleScore, transactionScore, accountScore, customerScore);
        
        return RiskScoreBreakdown.builder()
            .ruleBasedScore(ruleScore)
            .transactionScore(transactionScore)
            .accountScore(accountScore)
            .customerScore(customerScore)
            .totalScore(totalScore)
            .ruleWeight(BigDecimal.valueOf(ruleWeight))
            .transactionWeight(BigDecimal.valueOf(transactionWeight))
            .accountWeight(BigDecimal.valueOf(accountWeight))
            .customerWeight(BigDecimal.valueOf(customerWeight))
            .baseScore(BigDecimal.valueOf(baseScore))
            .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RiskScoreBreakdown {
        private BigDecimal ruleBasedScore;
        private BigDecimal transactionScore;
        private BigDecimal accountScore;
        private BigDecimal customerScore;
        private BigDecimal totalScore;
        private BigDecimal ruleWeight;
        private BigDecimal transactionWeight;
        private BigDecimal accountWeight;
        private BigDecimal customerWeight;
        private BigDecimal baseScore;
    }
}