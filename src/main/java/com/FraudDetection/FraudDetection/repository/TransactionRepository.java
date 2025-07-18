package com.FraudDetection.FraudDetection.repository;

import com.FraudDetection.FraudDetection.entity.Transaction;
import com.FraudDetection.FraudDetection.entity.TransactionStatus;
import com.FraudDetection.FraudDetection.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find by unique transaction reference
    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    // Find by account ID
    List<Transaction> findByAccountIdOrderByTimestampDesc(String accountId);
    Page<Transaction> findByAccountId(String accountId, Pageable pageable);
    
    // Find by status
    List<Transaction> findByStatus(TransactionStatus status);
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
    
    // Find by transaction type
    List<Transaction> findByTransactionType(TransactionType transactionType);
    
    // Find by amount range
    List<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find by date range
    List<Transaction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Transaction> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by account and date range
    List<Transaction> findByAccountIdAndTimestampBetween(String accountId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by merchant
    List<Transaction> findByMerchantIdOrderByTimestampDesc(String merchantId);
    Page<Transaction> findByMerchantId(String merchantId, Pageable pageable);
    
    // Find by location
    List<Transaction> findByLocationContainingIgnoreCase(String location);
    
    // Find by IP address
    List<Transaction> findByIpAddress(String ipAddress);
    
    // Find by device ID
    List<Transaction> findByDeviceId(String deviceId);
    
    // Custom queries for fraud detection
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<Transaction> findRecentTransactionsByAccount(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp >= :since")
    long countTransactionsByAccountSince(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp >= :since")
    BigDecimal sumTransactionAmountsByAccountSince(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.amount > :amount AND t.timestamp >= :since")
    List<Transaction> findLargeTransactionsByAccount(@Param("accountId") String accountId, @Param("amount") BigDecimal amount, @Param("since") LocalDateTime since);
    
    @Query("SELECT DISTINCT t.location FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp >= :since")
    List<String> findDistinctLocationsByAccount(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.location != :usualLocation AND t.timestamp >= :since")
    List<Transaction> findTransactionsFromUnusualLocations(@Param("accountId") String accountId, @Param("usualLocation") String usualLocation, @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Transaction t WHERE t.deviceId = :deviceId AND t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<Transaction> findTransactionsByDeviceSince(@Param("deviceId") String deviceId, @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Transaction t WHERE t.ipAddress = :ipAddress AND t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<Transaction> findTransactionsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Transaction t WHERE t.status IN :statuses ORDER BY t.timestamp DESC")
    List<Transaction> findByStatusIn(@Param("statuses") List<TransactionStatus> statuses);
    
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.transactionType = :type AND t.timestamp >= :since")
    List<Transaction> findByAccountAndTypeAndTimestampAfter(@Param("accountId") String accountId, @Param("type") TransactionType type, @Param("since") LocalDateTime since);
    
    // Statistics queries
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.timestamp >= :since")
    long countTransactionsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp >= :since")
    BigDecimal averageTransactionAmountByAccount(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT MAX(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.timestamp >= :since")
    BigDecimal maxTransactionAmountByAccount(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
}