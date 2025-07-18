package com.FraudDetection.FraudDetection.service.rules;

import com.FraudDetection.FraudDetection.entity.Transaction;
import com.FraudDetection.FraudDetection.repository.TransactionRepository;
import com.FraudDetection.FraudDetection.service.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class VelocityFraudRule extends AbstractFraudRule {
    
    private final TransactionRepository transactionRepository;
    
    @Value("${fraud.rules.velocity.max-transactions-per-hour:10}")
    private int maxTransactionsPerHour;
    
    @Value("${fraud.rules.velocity.max-transactions-per-day:50}")
    private int maxTransactionsPerDay;
    
    @Value("${fraud.rules.velocity.max-amount-per-hour:10000}")
    private BigDecimal maxAmountPerHour;
    
    @Value("${fraud.rules.velocity.max-amount-per-day:50000}")
    private BigDecimal maxAmountPerDay;
    
    public VelocityFraudRule(TransactionRepository transactionRepository) {
        super("VELOCITY_RULE", "1.0", "Detects unusual transaction velocity patterns", true, 80);
        this.transactionRepository = transactionRepository;
    }
    
    @Override
    protected RuleResult executeRule(Transaction transaction) {
        String accountId = transaction.getAccountId();
        LocalDateTime now = transaction.getTimestamp();
        
        // Check hourly velocity
        VelocityCheck hourlyCheck = checkHourlyVelocity(accountId, now);
        
        // Check daily velocity
        VelocityCheck dailyCheck = checkDailyVelocity(accountId, now);
        
        // Determine if rule is triggered
        boolean triggered = hourlyCheck.isViolated() || dailyCheck.isViolated();
        
        if (triggered) {
            BigDecimal score = calculateVelocityScore(hourlyCheck, dailyCheck);
            String reason = buildViolationReason(hourlyCheck, dailyCheck);
            
            Map<String, Object> ruleData = new HashMap<>();
            ruleData.put("hourlyTransactionCount", hourlyCheck.getTransactionCount());
            ruleData.put("hourlyAmount", hourlyCheck.getTotalAmount());
            ruleData.put("dailyTransactionCount", dailyCheck.getTransactionCount());
            ruleData.put("dailyAmount", dailyCheck.getTotalAmount());
            ruleData.put("accountId", accountId);
            
            return RuleResult.builder()
                .ruleName(ruleName)
                .triggered(true)
                .score(score)
                .reason(reason)
                .severity(determineSeverity(score))
                .ruleData(ruleData)
                .recommendation(getRecommendation(score))
                .build();
        }
        
        return createNotTriggeredResult();
    }
    
    private VelocityCheck checkHourlyVelocity(String accountId, LocalDateTime timestamp) {
        LocalDateTime oneHourAgo = timestamp.minusHours(1);
        
        List<Transaction> recentTransactions = transactionRepository
            .findByAccountIdAndTimestampBetween(accountId, oneHourAgo, timestamp);
        
        int transactionCount = recentTransactions.size();
        BigDecimal totalAmount = recentTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        boolean countViolated = transactionCount > maxTransactionsPerHour;
        boolean amountViolated = totalAmount.compareTo(maxAmountPerHour) > 0;
        
        return VelocityCheck.builder()
            .period("HOURLY")
            .transactionCount(transactionCount)
            .totalAmount(totalAmount)
            .maxTransactions(maxTransactionsPerHour)
            .maxAmount(maxAmountPerHour)
            .countViolated(countViolated)
            .amountViolated(amountViolated)
            .build();
    }
    
    private VelocityCheck checkDailyVelocity(String accountId, LocalDateTime timestamp) {
        LocalDateTime oneDayAgo = timestamp.minusDays(1);
        
        List<Transaction> recentTransactions = transactionRepository
            .findByAccountIdAndTimestampBetween(accountId, oneDayAgo, timestamp);
        
        int transactionCount = recentTransactions.size();
        BigDecimal totalAmount = recentTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        boolean countViolated = transactionCount > maxTransactionsPerDay;
        boolean amountViolated = totalAmount.compareTo(maxAmountPerDay) > 0;
        
        return VelocityCheck.builder()
            .period("DAILY")
            .transactionCount(transactionCount)
            .totalAmount(totalAmount)
            .maxTransactions(maxTransactionsPerDay)
            .maxAmount(maxAmountPerDay)
            .countViolated(countViolated)
            .amountViolated(amountViolated)
            .build();
    }
    
    private BigDecimal calculateVelocityScore(VelocityCheck hourlyCheck, VelocityCheck dailyCheck) {
        BigDecimal score = BigDecimal.ZERO;
        
        // Hourly violations are more severe
        if (hourlyCheck.isCountViolated()) {
            double ratio = (double) hourlyCheck.getTransactionCount() / hourlyCheck.getMaxTransactions();
            score = score.add(BigDecimal.valueOf(50 * ratio));
        }
        
        if (hourlyCheck.isAmountViolated()) {
            double ratio = hourlyCheck.getTotalAmount().divide(hourlyCheck.getMaxAmount(), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            score = score.add(BigDecimal.valueOf(40 * ratio));
        }
        
        // Daily violations are less severe but still significant
        if (dailyCheck.isCountViolated()) {
            double ratio = (double) dailyCheck.getTransactionCount() / dailyCheck.getMaxTransactions();
            score = score.add(BigDecimal.valueOf(30 * ratio));
        }
        
        if (dailyCheck.isAmountViolated()) {
            double ratio = dailyCheck.getTotalAmount().divide(dailyCheck.getMaxAmount(), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            score = score.add(BigDecimal.valueOf(25 * ratio));
        }
        
        // Cap the score at 100
        return score.min(BigDecimal.valueOf(100));
    }
    
    private String buildViolationReason(VelocityCheck hourlyCheck, VelocityCheck dailyCheck) {
        StringBuilder reason = new StringBuilder("Velocity violation detected: ");
        
        if (hourlyCheck.isCountViolated()) {
            reason.append(String.format("Hourly transaction count %d exceeds limit %d; ", 
                hourlyCheck.getTransactionCount(), hourlyCheck.getMaxTransactions()));
        }
        
        if (hourlyCheck.isAmountViolated()) {
            reason.append(String.format("Hourly amount %.2f exceeds limit %.2f; ", 
                hourlyCheck.getTotalAmount(), hourlyCheck.getMaxAmount()));
        }
        
        if (dailyCheck.isCountViolated()) {
            reason.append(String.format("Daily transaction count %d exceeds limit %d; ", 
                dailyCheck.getTransactionCount(), dailyCheck.getMaxTransactions()));
        }
        
        if (dailyCheck.isAmountViolated()) {
            reason.append(String.format("Daily amount %.2f exceeds limit %.2f; ", 
                dailyCheck.getTotalAmount(), dailyCheck.getMaxAmount()));
        }
        
        return reason.toString().trim();
    }
    
    private String getRecommendation(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "IMMEDIATE_REVIEW_REQUIRED";
        } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "ENHANCED_MONITORING";
        } else {
            return "STANDARD_MONITORING";
        }
    }
    
    @lombok.Data
    @lombok.Builder
    private static class VelocityCheck {
        private String period;
        private int transactionCount;
        private BigDecimal totalAmount;
        private int maxTransactions;
        private BigDecimal maxAmount;
        private boolean countViolated;
        private boolean amountViolated;
        
        public boolean isViolated() {
            return countViolated || amountViolated;
        }
    }
}