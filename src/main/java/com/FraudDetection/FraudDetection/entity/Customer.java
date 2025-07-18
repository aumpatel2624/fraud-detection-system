package com.FraudDetection.FraudDetection.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_email", columnList = "email"),
    @Index(name = "idx_customer_phone", columnList = "phoneNumber"),
    @Index(name = "idx_customer_ssn", columnList = "socialSecurityNumber"),
    @Index(name = "idx_customer_status", columnList = "status"),
    @Index(name = "idx_customer_created_at", columnList = "createdAt"),
    @Index(name = "idx_customer_risk_level", columnList = "riskLevel")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"accounts"})
@Schema(description = "Customer entity representing a bank customer")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique customer ID", example = "1")
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "Customer number is required")
    @Size(max = 20, message = "Customer number must not exceed 20 characters")
    @Schema(description = "Unique customer number", example = "CUST-001234")
    private String customerNumber;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Schema(description = "Customer's first name", example = "John")
    private String firstName;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Schema(description = "Customer's last name", example = "Doe")
    private String lastName;
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String email;
    
    @Column(nullable = false, length = 20)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Schema(description = "Customer's phone number", example = "+1234567890")
    private String phoneNumber;
    
    @Column(nullable = false)
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Customer's date of birth", example = "1990-05-15")
    private LocalDate dateOfBirth;
    
    @Column(unique = true, length = 11)
    @Size(max = 11, message = "Social Security Number must not exceed 11 characters")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "Invalid SSN format (xxx-xx-xxxx)")
    @Schema(description = "Customer's Social Security Number", example = "123-45-6789")
    private String socialSecurityNumber;
    
    @Column(nullable = false, length = 200)
    @NotBlank(message = "Address is required")
    @Size(max = 200, message = "Address must not exceed 200 characters")
    @Schema(description = "Customer's address", example = "123 Main St, Anytown, ST 12345")
    private String address;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must not exceed 50 characters")
    @Schema(description = "Customer's city", example = "New York")
    private String city;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must not exceed 50 characters")
    @Schema(description = "Customer's state", example = "NY")
    private String state;
    
    @Column(nullable = false, length = 10)
    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zip code format")
    @Size(max = 10, message = "Zip code must not exceed 10 characters")
    @Schema(description = "Customer's zip code", example = "12345")
    private String zipCode;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "Country is required")
    @Size(max = 50, message = "Country must not exceed 50 characters")
    @Schema(description = "Customer's country", example = "USA")
    private String country;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Customer's status", example = "ACTIVE")
    private CustomerStatus status = CustomerStatus.ACTIVE;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Customer's risk level", example = "LOW")
    private RiskLevel riskLevel = RiskLevel.LOW;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "When customer account was created", example = "2025-01-15 09:30:00")
    private LocalDateTime customerSince;
    
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last login timestamp", example = "2025-07-18 10:30:00")
    private LocalDateTime lastLogin;
    
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last transaction timestamp", example = "2025-07-18 09:45:00")
    private LocalDateTime lastTransactionDate;
    
    @Column(length = 500)
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Schema(description = "Additional notes about the customer", example = "VIP customer with preferred status")
    private String notes;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Schema(description = "Customer's accounts")
    private List<Account> accounts;
    
    // JPA Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.customerSince = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}