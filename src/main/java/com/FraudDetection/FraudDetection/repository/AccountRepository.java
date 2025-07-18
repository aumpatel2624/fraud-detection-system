package com.FraudDetection.FraudDetection.repository;

import com.FraudDetection.FraudDetection.entity.Account;
import com.FraudDetection.FraudDetection.entity.AccountStatus;
import com.FraudDetection.FraudDetection.entity.AccountType;
import com.FraudDetection.FraudDetection.entity.Customer;
import com.FraudDetection.FraudDetection.entity.RiskLevel;
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
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    // Find by unique account number
    Optional<Account> findByAccountNumber(String accountNumber);
    
    // Find by customer
    List<Account> findByCustomer(Customer customer);
    List<Account> findByCustomerId(Long customerId);
    
    // Find by customer and status
    List<Account> findByCustomerAndStatus(Customer customer, AccountStatus status);
    List<Account> findByCustomerIdAndStatus(Long customerId, AccountStatus status);
    
    // Find by account type
    List<Account> findByAccountType(AccountType accountType);
    Page<Account> findByAccountType(AccountType accountType, Pageable pageable);
    
    // Find by status
    List<Account> findByStatus(AccountStatus status);
    Page<Account> findByStatus(AccountStatus status, Pageable pageable);
    
    // Find by risk level
    List<Account> findByRiskLevel(RiskLevel riskLevel);
    Page<Account> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);
    
    // Find by balance range
    List<Account> findByBalanceBetween(BigDecimal minBalance, BigDecimal maxBalance);
    
    // Find by available balance range
    List<Account> findByAvailableBalanceBetween(BigDecimal minBalance, BigDecimal maxBalance);
    
    // Find by currency
    List<Account> findByCurrency(String currency);
    
    // Find by branch code
    List<Account> findByBranchCode(String branchCode);
    
    // Find by account manager
    List<Account> findByAccountManager(String accountManager);
    
    // Find flagged accounts
    List<Account> findByFlaggedForMonitoring(Boolean flagged);
    Page<Account> findByFlaggedForMonitoring(Boolean flagged, Pageable pageable);
    
    // Find by opened date range
    List<Account> findByOpenedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by last transaction date
    List<Account> findByLastTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find active accounts
    @Query("SELECT a FROM Account a WHERE a.status = 'ACTIVE' ORDER BY a.openedAt DESC")
    List<Account> findActiveAccounts();
    
    @Query("SELECT a FROM Account a WHERE a.status = 'ACTIVE' ORDER BY a.openedAt DESC")
    Page<Account> findActiveAccounts(Pageable pageable);
    
    // Find high-risk accounts
    @Query("SELECT a FROM Account a WHERE a.riskLevel IN ('HIGH', 'VERY_HIGH') ORDER BY a.riskLevel DESC")
    List<Account> findHighRiskAccounts();
    
    // Find accounts with high balances
    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance ORDER BY a.balance DESC")
    List<Account> findHighBalanceAccounts(@Param("minBalance") BigDecimal minBalance);
    
    // Find accounts with low balances
    @Query("SELECT a FROM Account a WHERE a.balance <= :maxBalance AND a.status = 'ACTIVE' ORDER BY a.balance ASC")
    List<Account> findLowBalanceAccounts(@Param("maxBalance") BigDecimal maxBalance);
    
    // Find accounts by customer and type
    List<Account> findByCustomerAndAccountType(Customer customer, AccountType accountType);
    List<Account> findByCustomerIdAndAccountType(Long customerId, AccountType accountType);
    
    // Find accounts by multiple criteria
    @Query("SELECT a FROM Account a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:accountType IS NULL OR a.accountType = :accountType) AND " +
           "(:riskLevel IS NULL OR a.riskLevel = :riskLevel) AND " +
           "(:currency IS NULL OR a.currency = :currency) AND " +
           "(:flagged IS NULL OR a.flaggedForMonitoring = :flagged) " +
           "ORDER BY a.openedAt DESC")
    Page<Account> findByCriteria(
        @Param("status") AccountStatus status,
        @Param("accountType") AccountType accountType,
        @Param("riskLevel") RiskLevel riskLevel,
        @Param("currency") String currency,
        @Param("flagged") Boolean flagged,
        Pageable pageable
    );
    
    // Find accounts without recent transactions
    @Query("SELECT a FROM Account a WHERE a.lastTransactionDate < :cutoffDate OR a.lastTransactionDate IS NULL")
    List<Account> findAccountsWithoutRecentTransactions(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find accounts by customer risk level
    @Query("SELECT a FROM Account a WHERE a.customer.riskLevel = :riskLevel")
    List<Account> findAccountsByCustomerRiskLevel(@Param("riskLevel") RiskLevel riskLevel);
    
    // Find new accounts
    @Query("SELECT a FROM Account a WHERE a.openedAt >= :since ORDER BY a.openedAt DESC")
    List<Account> findNewAccounts(@Param("since") LocalDateTime since);
    
    // Find closed accounts
    @Query("SELECT a FROM Account a WHERE a.status = 'CLOSED' AND a.closedAt IS NOT NULL ORDER BY a.closedAt DESC")
    List<Account> findClosedAccounts();
    
    // Find accounts by balance and currency
    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance AND a.currency = :currency ORDER BY a.balance DESC")
    List<Account> findAccountsByBalanceAndCurrency(@Param("minBalance") BigDecimal minBalance, @Param("currency") String currency);
    
    // Search accounts by account number or customer info
    @Query("SELECT a FROM Account a WHERE " +
           "a.accountNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(a.customer.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.customer.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.customer.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Account> searchAccounts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Count accounts by status
    long countByStatus(AccountStatus status);
    
    // Count accounts by type
    long countByAccountType(AccountType accountType);
    
    // Count accounts by risk level
    long countByRiskLevel(RiskLevel riskLevel);
    
    // Count accounts by currency
    long countByCurrency(String currency);
    
    // Count flagged accounts
    long countByFlaggedForMonitoring(Boolean flagged);
    
    // Count accounts by customer
    long countByCustomer(Customer customer);
    long countByCustomerId(Long customerId);
    
    // Count new accounts since date
    @Query("SELECT COUNT(a) FROM Account a WHERE a.openedAt >= :since")
    long countNewAccountsSince(@Param("since") LocalDateTime since);
    
    // Calculate total balance by currency
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.currency = :currency AND a.status = 'ACTIVE'")
    BigDecimal totalBalanceByCurrency(@Param("currency") String currency);
    
    // Calculate average balance by account type
    @Query("SELECT AVG(a.balance) FROM Account a WHERE a.accountType = :accountType AND a.status = 'ACTIVE'")
    BigDecimal averageBalanceByAccountType(@Param("accountType") AccountType accountType);
    
    // Check if account number exists
    boolean existsByAccountNumber(String accountNumber);
}