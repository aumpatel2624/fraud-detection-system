package com.FraudDetection.FraudDetection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Health monitoring endpoints for the Fraud Detection Service")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping
    @Operation(summary = "Basic health check", description = "Returns basic health status of the service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Fraud Detection Service");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/database")
    @Operation(summary = "Database health check", description = "Returns database connection status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Database is healthy"),
        @ApiResponse(responseCode = "503", description = "Database is down")
    })
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            dbHealth.put("status", "UP");
            dbHealth.put("database", "PostgreSQL");
            dbHealth.put("url", connection.getMetaData().getURL());
            dbHealth.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
            dbHealth.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(503).body(dbHealth);
        }
        
        return ResponseEntity.ok(dbHealth);
    }
    
    @GetMapping("/detailed")
    @Operation(summary = "Detailed health check", description = "Returns comprehensive health information including system details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detailed health information retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("application", "Fraud Detection Service");
        health.put("version", "1.0.0");
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        
        // Database check
        Map<String, Object> dbStatus = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            dbStatus.put("status", "UP");
            dbStatus.put("database", "PostgreSQL");
            dbStatus.put("driver", connection.getMetaData().getDriverName());
            dbStatus.put("version", connection.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            dbStatus.put("status", "DOWN");
            dbStatus.put("error", e.getMessage());
        }
        
        health.put("database", dbStatus);
        
        // System info
        Map<String, Object> system = new HashMap<>();
        system.put("java_version", System.getProperty("java.version"));
        system.put("os_name", System.getProperty("os.name"));
        system.put("os_version", System.getProperty("os.version"));
        system.put("available_processors", Runtime.getRuntime().availableProcessors());
        system.put("max_memory", Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        
        health.put("system", system);
        
        return ResponseEntity.ok(health);
    }
}