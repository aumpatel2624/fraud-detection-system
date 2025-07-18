package com.FraudDetection.FraudDetection.repository;

import com.FraudDetection.FraudDetection.entity.FraudAlert;
import com.FraudDetection.FraudDetection.entity.FraudAlertStatus;
import com.FraudDetection.FraudDetection.entity.FraudSeverity;
import com.FraudDetection.FraudDetection.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    
    // Find by transaction
    List<FraudAlert> findByTransaction(Transaction transaction);
    List<FraudAlert> findByTransactionId(Long transactionId);
    
    // Find by status
    List<FraudAlert> findByStatus(FraudAlertStatus status);
    Page<FraudAlert> findByStatus(FraudAlertStatus status, Pageable pageable);
    
    // Find by severity
    List<FraudAlert> findBySeverity(FraudSeverity severity);
    Page<FraudAlert> findBySeverity(FraudSeverity severity, Pageable pageable);
    
    // Find by rule type
    List<FraudAlert> findByRuleType(String ruleType);
    Page<FraudAlert> findByRuleType(String ruleType, Pageable pageable);
    
    // Find by assigned user
    List<FraudAlert> findByAssignedTo(String assignedTo);
    Page<FraudAlert> findByAssignedTo(String assignedTo, Pageable pageable);
    
    // Find by resolved user
    List<FraudAlert> findByResolvedBy(String resolvedBy);
    
    // Find by date range
    List<FraudAlert> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<FraudAlert> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by risk score range
    List<FraudAlert> findByRiskScoreBetween(BigDecimal minScore, BigDecimal maxScore);
    
    // Find by confidence score range
    List<FraudAlert> findByConfidenceScoreBetween(BigDecimal minScore, BigDecimal maxScore);
    
    // Find active alerts
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.status = 'ACTIVE' ORDER BY fa.severity DESC, fa.riskScore DESC")
    List<FraudAlert> findActiveAlerts();
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.status = 'ACTIVE' ORDER BY fa.severity DESC, fa.riskScore DESC")
    Page<FraudAlert> findActiveAlerts(Pageable pageable);
    
    // Find high-risk alerts
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.riskScore >= :minRiskScore AND fa.status IN :statuses ORDER BY fa.riskScore DESC")
    List<FraudAlert> findHighRiskAlerts(@Param("minRiskScore") BigDecimal minRiskScore, @Param("statuses") List<FraudAlertStatus> statuses);
    
    // Find alerts by account
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.transaction.accountId = :accountId ORDER BY fa.createdAt DESC")
    List<FraudAlert> findByAccountId(@Param("accountId") String accountId);
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.transaction.accountId = :accountId ORDER BY fa.createdAt DESC")
    Page<FraudAlert> findByAccountId(@Param("accountId") String accountId, Pageable pageable);
    
    // Find unresolved alerts
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.status NOT IN ('RESOLVED', 'DISMISSED', 'CLOSED') ORDER BY fa.severity DESC, fa.createdAt ASC")
    List<FraudAlert> findUnresolvedAlerts();
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.status NOT IN ('RESOLVED', 'DISMISSED', 'CLOSED') ORDER BY fa.severity DESC, fa.createdAt ASC")
    Page<FraudAlert> findUnresolvedAlerts(Pageable pageable);
    
    // Find alerts by severity and status
    List<FraudAlert> findBySeverityAndStatus(FraudSeverity severity, FraudAlertStatus status);
    
    // Find alerts needing attention (active, escalated, under investigation)
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.status IN ('ACTIVE', 'ESCALATED', 'UNDER_INVESTIGATION') ORDER BY fa.severity DESC, fa.riskScore DESC")
    List<FraudAlert> findAlertsNeedingAttention();
    
    // Find alerts by date range and status
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.createdAt BETWEEN :startDate AND :endDate AND fa.status = :status")
    List<FraudAlert> findByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") FraudAlertStatus status);
    
    // Count alerts by status
    long countByStatus(FraudAlertStatus status);
    
    // Count alerts by severity
    long countBySeverity(FraudSeverity severity);
    
    // Count alerts by rule type
    long countByRuleType(String ruleType);
    
    // Count alerts by account
    @Query("SELECT COUNT(fa) FROM FraudAlert fa WHERE fa.transaction.accountId = :accountId")
    long countByAccountId(@Param("accountId") String accountId);
    
    // Count alerts by account and date range
    @Query("SELECT COUNT(fa) FROM FraudAlert fa WHERE fa.transaction.accountId = :accountId AND fa.createdAt >= :since")
    long countByAccountIdSince(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    // Find recent alerts by account
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.transaction.accountId = :accountId AND fa.createdAt >= :since ORDER BY fa.createdAt DESC")
    List<FraudAlert> findRecentAlertsByAccount(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    // Find alerts by multiple criteria
    @Query("SELECT fa FROM FraudAlert fa WHERE " +
           "(:severity IS NULL OR fa.severity = :severity) AND " +
           "(:status IS NULL OR fa.status = :status) AND " +
           "(:ruleType IS NULL OR fa.ruleType = :ruleType) AND " +
           "(:assignedTo IS NULL OR fa.assignedTo = :assignedTo) " +
           "ORDER BY fa.createdAt DESC")
    Page<FraudAlert> findByCriteria(
        @Param("severity") FraudSeverity severity,
        @Param("status") FraudAlertStatus status,
        @Param("ruleType") String ruleType,
        @Param("assignedTo") String assignedTo,
        Pageable pageable
    );
    
    // Statistics
    @Query("SELECT AVG(fa.riskScore) FROM FraudAlert fa WHERE fa.createdAt >= :since")
    BigDecimal averageRiskScoreSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT MAX(fa.riskScore) FROM FraudAlert fa WHERE fa.createdAt >= :since")
    BigDecimal maxRiskScoreSince(@Param("since") LocalDateTime since);
}