package com.FraudDetection.FraudDetection.repository;

import com.FraudDetection.FraudDetection.entity.Customer;
import com.FraudDetection.FraudDetection.entity.CustomerStatus;
import com.FraudDetection.FraudDetection.entity.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // Find by unique customer number
    Optional<Customer> findByCustomerNumber(String customerNumber);
    
    // Find by email
    Optional<Customer> findByEmail(String email);
    
    // Find by phone number
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    
    // Find by SSN
    Optional<Customer> findBySocialSecurityNumber(String socialSecurityNumber);
    
    // Find by name
    List<Customer> findByFirstNameIgnoreCase(String firstName);
    List<Customer> findByLastNameIgnoreCase(String lastName);
    List<Customer> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
    
    // Find by name containing
    List<Customer> findByFirstNameContainingIgnoreCase(String firstName);
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
    
    // Find by status
    List<Customer> findByStatus(CustomerStatus status);
    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);
    
    // Find by risk level
    List<Customer> findByRiskLevel(RiskLevel riskLevel);
    Page<Customer> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);
    
    // Find by date of birth
    List<Customer> findByDateOfBirth(LocalDate dateOfBirth);
    
    // Find by date of birth range
    List<Customer> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);
    
    // Find by location
    List<Customer> findByCityIgnoreCase(String city);
    List<Customer> findByStateIgnoreCase(String state);
    List<Customer> findByCountryIgnoreCase(String country);
    List<Customer> findByZipCode(String zipCode);
    
    // Find by location containing
    List<Customer> findByAddressContainingIgnoreCase(String address);
    
    // Find by customer since date
    List<Customer> findByCustomerSinceBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by last login
    List<Customer> findByLastLoginBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find active customers
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' ORDER BY c.customerSince DESC")
    List<Customer> findActiveCustomers();
    
    @Query("SELECT c FROM Customer c WHERE c.status = 'ACTIVE' ORDER BY c.customerSince DESC")
    Page<Customer> findActiveCustomers(Pageable pageable);
    
    // Find high-risk customers
    @Query("SELECT c FROM Customer c WHERE c.riskLevel IN ('HIGH', 'VERY_HIGH') ORDER BY c.riskLevel DESC")
    List<Customer> findHighRiskCustomers();
    
    // Find customers by multiple criteria
    @Query("SELECT c FROM Customer c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:riskLevel IS NULL OR c.riskLevel = :riskLevel) AND " +
           "(:city IS NULL OR LOWER(c.city) = LOWER(:city)) AND " +
           "(:state IS NULL OR LOWER(c.state) = LOWER(:state)) " +
           "ORDER BY c.customerSince DESC")
    Page<Customer> findByCriteria(
        @Param("status") CustomerStatus status,
        @Param("riskLevel") RiskLevel riskLevel,
        @Param("city") String city,
        @Param("state") String state,
        Pageable pageable
    );
    
    // Find customers who haven't logged in recently
    @Query("SELECT c FROM Customer c WHERE c.lastLogin < :cutoffDate OR c.lastLogin IS NULL ORDER BY c.lastLogin ASC")
    List<Customer> findCustomersWithoutRecentLogin(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find customers by age range
    @Query("SELECT c FROM Customer c WHERE c.dateOfBirth BETWEEN :startDate AND :endDate")
    List<Customer> findCustomersByAgeRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Find new customers
    @Query("SELECT c FROM Customer c WHERE c.customerSince >= :since ORDER BY c.customerSince DESC")
    List<Customer> findNewCustomers(@Param("since") LocalDateTime since);
    
    // Find customers by risk and status
    List<Customer> findByRiskLevelAndStatus(RiskLevel riskLevel, CustomerStatus status);
    
    // Search customers by name or email
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.customerNumber LIKE CONCAT('%', :searchTerm, '%')")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Count customers by status
    long countByStatus(CustomerStatus status);
    
    // Count customers by risk level
    long countByRiskLevel(RiskLevel riskLevel);
    
    // Count customers by location
    long countByCity(String city);
    long countByState(String state);
    long countByCountry(String country);
    
    // Count new customers since date
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.customerSince >= :since")
    long countNewCustomersSince(@Param("since") LocalDateTime since);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Check if phone number exists
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Check if SSN exists
    boolean existsBySocialSecurityNumber(String socialSecurityNumber);
    
    // Check if customer number exists
    boolean existsByCustomerNumber(String customerNumber);
}