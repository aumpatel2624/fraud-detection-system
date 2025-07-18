package com.FraudDetection.FraudDetection.repository;

import com.FraudDetection.FraudDetection.entity.AuditLog;
import com.FraudDetection.FraudDetection.entity.AuditSeverity;
import com.FraudDetection.FraudDetection.entity.FraudAlert;
import com.FraudDetection.FraudDetection.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find by transaction
    List<AuditLog> findByTransaction(Transaction transaction);
    List<AuditLog> findByTransactionId(Long transactionId);
    
    // Find by fraud alert
    List<AuditLog> findByFraudAlert(FraudAlert fraudAlert);
    List<AuditLog> findByFraudAlertId(Long fraudAlertId);
    
    // Find by entity type
    List<AuditLog> findByEntityType(String entityType);
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
    
    // Find by entity ID
    List<AuditLog> findByEntityId(String entityId);
    
    // Find by action
    List<AuditLog> findByAction(String action);
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    // Find by user
    List<AuditLog> findByPerformedBy(String performedBy);
    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);
    
    // Find by user role
    List<AuditLog> findByUserRole(String userRole);
    
    // Find by source system
    List<AuditLog> findBySourceSystem(String sourceSystem);
    
    // Find by IP address
    List<AuditLog> findByIpAddress(String ipAddress);
    
    // Find by severity
    List<AuditLog> findBySeverity(AuditSeverity severity);
    Page<AuditLog> findBySeverity(AuditSeverity severity, Pageable pageable);
    
    // Find by event category
    List<AuditLog> findByEventCategory(String eventCategory);
    Page<AuditLog> findByEventCategory(String eventCategory, Pageable pageable);
    
    // Find by success status
    List<AuditLog> findBySuccessful(Boolean successful);
    Page<AuditLog> findBySuccessful(Boolean successful, Pageable pageable);
    
    // Find by date range
    List<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find recent logs
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt >= :since ORDER BY al.createdAt DESC")
    List<AuditLog> findRecentLogs(@Param("since") LocalDateTime since);
    
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt >= :since ORDER BY al.createdAt DESC")
    Page<AuditLog> findRecentLogs(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Find logs by entity type and action
    List<AuditLog> findByEntityTypeAndAction(String entityType, String action);
    
    // Find logs by user and date range
    @Query("SELECT al FROM AuditLog al WHERE al.performedBy = :user AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findByUserAndDateRange(@Param("user") String user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find error logs
    @Query("SELECT al FROM AuditLog al WHERE al.successful = false OR al.severity = 'ERROR' ORDER BY al.createdAt DESC")
    List<AuditLog> findErrorLogs();
    
    @Query("SELECT al FROM AuditLog al WHERE al.successful = false OR al.severity = 'ERROR' ORDER BY al.createdAt DESC")
    Page<AuditLog> findErrorLogs(Pageable pageable);
    
    // Find critical logs
    @Query("SELECT al FROM AuditLog al WHERE al.severity = 'CRITICAL' ORDER BY al.createdAt DESC")
    List<AuditLog> findCriticalLogs();
    
    // Find logs by entity and action
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId ORDER BY al.createdAt DESC")
    List<AuditLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") String entityId);
    
    // Find logs by multiple criteria
    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:entityType IS NULL OR al.entityType = :entityType) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:performedBy IS NULL OR al.performedBy = :performedBy) AND " +
           "(:severity IS NULL OR al.severity = :severity) AND " +
           "(:successful IS NULL OR al.successful = :successful) AND " +
           "al.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY al.createdAt DESC")
    Page<AuditLog> findByCriteria(
        @Param("entityType") String entityType,
        @Param("action") String action,
        @Param("performedBy") String performedBy,
        @Param("severity") AuditSeverity severity,
        @Param("successful") Boolean successful,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    // Count logs by entity type
    long countByEntityType(String entityType);
    
    // Count logs by action
    long countByAction(String action);
    
    // Count logs by user
    long countByPerformedBy(String performedBy);
    
    // Count logs by severity
    long countBySeverity(AuditSeverity severity);
    
    // Count logs by date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count error logs
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.successful = false OR al.severity = 'ERROR'")
    long countErrorLogs();
    
    // Count logs by user and date range
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.performedBy = :user AND al.createdAt BETWEEN :startDate AND :endDate")
    long countByUserAndDateRange(@Param("user") String user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find logs with tags
    @Query("SELECT al FROM AuditLog al WHERE al.tags LIKE %:tag% ORDER BY al.createdAt DESC")
    List<AuditLog> findByTag(@Param("tag") String tag);
    
    // Find activity by IP address in date range
    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ipAddress AND al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findActivityByIpAndDateRange(@Param("ipAddress") String ipAddress, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}