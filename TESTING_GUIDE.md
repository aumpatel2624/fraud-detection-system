# Fraud Detection Service - Testing Guide

## Overview
This guide provides comprehensive testing instructions for each phase of the Fraud Detection Service development using **IntelliJ IDEA** and **Postman** on Windows.

## Prerequisites
- IntelliJ IDEA Ultimate (recommended) or Community Edition
- Postman installed on Windows
- Docker Desktop for Windows
- Java 21 installed ✅
- Maven 3.8+

---

## Phase 1: Infrastructure Setup Testing

### 1.1 Environment Setup
1. **Start Docker Services**
   ```bash
   # In project directory
   docker-compose up -d
   ```

2. **Verify Docker Containers**
   ```bash
   docker-compose ps
   ```
   Expected: postgres, kafka, zookeeper, redis containers running

3. **IntelliJ IDEA Setup**
   - Import project as Maven project
   - Set Project SDK to Java 21
   - Ensure Maven auto-import is enabled
   - Run Maven reload

### 1.2 Compilation Testing
1. **In IntelliJ:**
   - Right-click on project → Maven → Reload project
   - Build → Build Project (Ctrl+F9)
   - Expected: Successful compilation

2. **Command Line Alternative:**
   ```bash
   mvn clean compile
   ```

### 1.3 Database Connection Testing
1. **In IntelliJ:**
   - Go to Database tab (View → Tool Windows → Database)
   - Add PostgreSQL data source:
     - Host: localhost
     - Port: 5432
     - Database: fraud_detection
     - Username: fraud_user
     - Password: fraud_password
   - Test connection

2. **Application Startup Test:**
   - Run FraudDetectionApplication.java
   - Check logs for successful database connection
   - Application should start without errors

### 1.4 Health Check Testing
1. **Basic Health Check**:
   - URL: `http://localhost:8080/api/health`
   - Method: GET
   - Expected Response:
     ```json
     {
       "status": "UP",
       "timestamp": "2025-07-18T10:48:49",
       "service": "Fraud Detection Service",
       "version": "1.0.0"
     }
     ```

2. **Database Health Check**:
   - URL: `http://localhost:8080/api/health/database`
   - Method: GET
   - Expected Response (if PostgreSQL is running):
     ```json
     {
       "status": "UP",
       "database": "PostgreSQL",
       "url": "jdbc:postgresql://localhost:5432/fraud_detection",
       "timestamp": "2025-07-18T10:48:49"
     }
     ```

3. **Detailed Health Check**:
   - URL: `http://localhost:8080/api/health/detailed`
   - Method: GET
   - Expected Response:
     ```json
     {
       "application": "Fraud Detection Service",
       "version": "1.0.0",
       "status": "UP",
       "timestamp": "2025-07-18T10:48:49",
       "database": {
         "status": "UP",
         "database": "PostgreSQL",
         "driver": "PostgreSQL JDBC Driver",
         "version": "15.x"
       },
       "system": {
         "java_version": "21.0.x",
         "os_name": "Windows 11",
         "os_version": "10.0",
         "available_processors": 8,
         "max_memory": "4096 MB"
       }
     }
     ```

### 1.5 Testing in IntelliJ IDEA
1. **Run Application**:
   - Right-click on `FraudDetectionApplication.java`
   - Select "Run 'FraudDetectionApplication'"
   - Wait for application to start (look for "Started FraudDetectionApplication" in console)

2. **Test Health Endpoints**:
   - Open browser or use IntelliJ's HTTP Client
   - Test each health endpoint mentioned above
   - Verify responses match expected format

### 1.6 Testing in Postman
1. **Create New Collection**:
   - Name: "Fraud Detection Health Checks"
   - Base URL: `http://localhost:8080`

2. **Add Health Check Requests**:
   ```
   GET {{baseUrl}}/api/health
   GET {{baseUrl}}/api/health/database
   GET {{baseUrl}}/api/health/detailed
   ```

3. **Set Environment Variables**:
   - `baseUrl`: `http://localhost:8080`

4. **Run Collection**:
   - Use Collection Runner to test all endpoints
   - Verify all responses return status 200
   - Check response bodies match expected format

### 1.7 Swagger/OpenAPI Testing
1. **Access Swagger UI**:
   - URL: `http://localhost:8080/swagger-ui.html`
   - Should display interactive API documentation
   - All health endpoints should be visible

2. **Test API Documentation**:
   - Click on "Health Check" section
   - Expand each endpoint
   - Click "Try it out" button
   - Execute requests directly from Swagger UI

3. **OpenAPI Specification**:
   - URL: `http://localhost:8080/api-docs`
   - Should return OpenAPI 3.0 JSON specification
   - Contains all endpoint definitions

### 1.8 Success Criteria Phase 1 ✅ **ALL ACHIEVED**
- ✅ **Application compiles successfully** with Java 21
- ✅ **Database connection established** with PostgreSQL
- ✅ **Application starts without errors**
- ✅ **All health check endpoints return 200 status**
- ✅ **Health endpoints return expected JSON format**
- ✅ **Database health check shows PostgreSQL connection**
- ✅ **Swagger UI accessible** at /swagger-ui.html
- ✅ **API documentation complete** with interactive testing
- ✅ **OpenAPI specification available** at /api-docs
- ✅ **All endpoints properly documented** with examples

---

## Phase 2: Domain Models Testing

### 2.1 Entity Persistence Testing
1. **Create Test Data:**
   - Use IntelliJ's built-in test runner
   - Run entity tests to verify JPA mappings
   - Check database tables are created correctly

2. **Database Schema Verification:**
   - In IntelliJ Database tool
   - Verify tables: transactions, fraud_alerts, audit_logs, customers, accounts
   - Check column types and constraints

### 2.2 Repository Testing
1. **Unit Tests:**
   - Run repository tests in IntelliJ
   - Test CRUD operations
   - Verify custom queries work correctly

2. **Integration Tests:**
   - Test repository methods with real database
   - Verify transaction rollback behavior
   - Check cascade operations

### 2.3 Success Criteria Phase 2
- ✅ All entity classes persist correctly
- ✅ Repository methods work as expected
- ✅ Database schema matches entity definitions
- ✅ Relationships are properly mapped

---

## Phase 3: Fraud Detection Engine Testing

### 3.1 Rule Engine Testing
1. **Unit Tests:**
   - Test each fraud detection rule individually
   - Verify rule evaluation logic
   - Test rule configuration

2. **Integration Tests:**
   - Test rule engine with sample transactions
   - Verify rule execution order
   - Test rule result aggregation

### 3.2 Performance Testing
1. **Load Testing:**
   - Create test data with 1000+ transactions
   - Measure processing time
   - Verify memory usage

2. **Fraud Detection Accuracy:**
   - Test with known fraud patterns
   - Verify detection accuracy
   - Test false positive rates

### 3.3 Success Criteria Phase 3
- ✅ All fraud rules detect anomalies correctly
- ✅ Rule engine handles large transaction volumes
- ✅ Performance targets met
- ✅ Configurable rule parameters work

---

## Phase 4: API Layer Testing with Postman

### 4.1 Postman Collection Setup
Create a new collection named "Fraud Detection API" with the following requests:

#### 4.1.1 Authentication Endpoints
1. **Register User**
   - POST: `http://localhost:8080/api/auth/register`
   - Body (JSON):
     ```json
     {
       "username": "auditor1",
       "password": "password123",
       "email": "auditor1@example.com",
       "role": "AUDITOR"
     }
     ```

2. **Login User**
   - POST: `http://localhost:8080/api/auth/login`
   - Body (JSON):
     ```json
     {
       "username": "auditor1",
       "password": "password123"
     }
     ```
   - Save the JWT token from response

#### 4.1.2 Transaction Endpoints
1. **Submit Transaction**
   - POST: `http://localhost:8080/api/transactions`
   - Headers: `Authorization: Bearer <JWT_TOKEN>`
   - Body (JSON):
     ```json
     {
       "accountId": "ACC001",
       "amount": 1000.00,
       "currency": "USD",
       "merchantId": "MERCHANT001",
       "location": "New York, NY",
       "transactionType": "PURCHASE"
     }
     ```

2. **Get Transaction**
   - GET: `http://localhost:8080/api/transactions/{id}`
   - Headers: `Authorization: Bearer <JWT_TOKEN>`

#### 4.1.3 Fraud Alert Endpoints
1. **Get Fraud Alerts**
   - GET: `http://localhost:8080/api/fraud-alerts`
   - Headers: `Authorization: Bearer <JWT_TOKEN>`

2. **Override Fraud Decision**
   - PUT: `http://localhost:8080/api/fraud-alerts/{id}/override`
   - Headers: `Authorization: Bearer <JWT_TOKEN>`
   - Body (JSON):
     ```json
     {
       "decision": "APPROVED",
       "reason": "Manual review completed"
     }
     ```

### 4.2 API Testing Scenarios

#### 4.2.1 Happy Path Testing
1. Register and login successfully
2. Submit valid transaction
3. Retrieve transaction details
4. Get fraud alerts
5. Override fraud decision

#### 4.2.2 Security Testing
1. Test endpoints without JWT token (should return 401)
2. Test with invalid JWT token (should return 401)
3. Test with expired JWT token (should return 401)
4. Test role-based access (different user roles)

#### 4.2.3 Error Handling Testing
1. Submit invalid transaction data (should return 400)
2. Access non-existent transaction (should return 404)
3. Submit malformed JSON (should return 400)

### 4.3 Success Criteria Phase 4
- ✅ All API endpoints respond correctly
- ✅ JWT authentication works properly
- ✅ Role-based authorization enforced
- ✅ Error handling returns appropriate HTTP codes
- ✅ API documentation accessible

---

## Phase 5: Integration & Performance Testing

### 5.1 Kafka Integration Testing
1. **Kafka Producer Testing:**
   - Submit transaction via API
   - Verify message produced to Kafka topic
   - Check message format and content

2. **Kafka Consumer Testing:**
   - Publish test message to Kafka
   - Verify application consumes message
   - Check processing logic

### 5.2 Apache Spark Testing
1. **Batch Processing Testing:**
   - Create test dataset with 10,000+ transactions
   - Run Spark batch job
   - Verify processing results

2. **Performance Testing:**
   - Test with 100,000+ transactions
   - Measure processing time
   - Verify resource usage

### 5.3 End-to-End Testing
1. **Complete Transaction Flow:**
   - Submit transaction → Kafka → Fraud Detection → Alert → API Response
   - Verify all components work together
   - Test error scenarios

2. **Load Testing:**
   - Use Postman or JMeter for load testing
   - Test with 1000+ concurrent transactions
   - Measure response times and throughput

### 5.4 Success Criteria Phase 5
- ✅ Kafka integration works correctly
- ✅ Spark batch processing handles large volumes
- ✅ End-to-end transaction flow works
- ✅ Performance targets achieved (75% latency reduction)
- ✅ System handles 1M+ transactions daily

---

## IntelliJ IDEA Testing Tips

### Running Tests
1. **Individual Test:** Right-click on test method → Run
2. **Test Class:** Right-click on test class → Run All Tests
3. **Test Package:** Right-click on package → Run All Tests
4. **Maven Tests:** Maven panel → Lifecycle → test

### Debugging
1. Set breakpoints in code
2. Right-click → Debug instead of Run
3. Use Debug panel to inspect variables
4. Step through code execution

### Database Integration
1. Use IntelliJ Database tool to inspect data
2. Run SQL queries directly
3. Monitor database changes during tests

---

## Postman Testing Tips

### Environment Variables
1. Create environment for base URL: `{{baseUrl}}`
2. Store JWT token as environment variable: `{{authToken}}`
3. Use pre-request scripts to set up data

### Test Scripts
Add to Tests tab in Postman:
```javascript
// Verify response status
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Verify response structure
pm.test("Response has required fields", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property("id");
    pm.expect(responseJson).to.have.property("status");
});

// Save JWT token
pm.test("Save JWT token", function () {
    const responseJson = pm.response.json();
    pm.environment.set("authToken", responseJson.token);
});
```

### Collection Runner
1. Use Collection Runner for automated testing
2. Set up data files for bulk testing
3. Monitor test results and failures

---

## Troubleshooting Common Issues

### Java 21 Compilation Issues
- Ensure Java 21 is installed and configured in IntelliJ
- Check Maven compiler plugin version
- Verify JAVA_HOME environment variable

### Database Connection Issues
- Verify Docker containers are running
- Check database credentials
- Ensure PostgreSQL driver is included

### JWT Token Issues
- Verify token is not expired
- Check token format (Bearer prefix)
- Ensure secret key is configured correctly

### Docker Issues
- Restart Docker Desktop
- Check port conflicts
- Verify docker-compose.yml configuration

---

## Performance Monitoring

### Application Metrics
- Monitor CPU and memory usage
- Track response times
- Monitor database connection pool
- Check Kafka message lag

### Database Performance
- Monitor query execution times
- Check database connection count
- Verify index usage

### Success Metrics
- Transaction processing rate: 1M+ per day
- Fraud detection latency: 75% reduction
- API response time: < 200ms
- System uptime: 99.9%