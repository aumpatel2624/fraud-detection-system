# PostgreSQL Database Setup with pgAdmin

## Step 1: Open pgAdmin and Connect to Server

1. **Open pgAdmin** on Windows
2. **Connect to PostgreSQL server** (usually localhost)
3. **Enter your PostgreSQL credentials**

## Step 2: Create Database and User

### Create Database
Right-click on "Databases" → "Create" → "Database"
- **Database Name**: `fraud_detection`
- **Owner**: postgres (or your current user)
- **Click Save**

### OR Use SQL Commands

In pgAdmin Query Tool, run these commands:

```sql
-- Create database
CREATE DATABASE fraud_detection;

-- Create user with password
CREATE USER fraud_user WITH PASSWORD 'fraud_password';

-- Grant all privileges to user on database
GRANT ALL PRIVILEGES ON DATABASE fraud_detection TO fraud_user;

-- Connect to the fraud_detection database
\c fraud_detection;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO fraud_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fraud_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fraud_user;
```

## Step 3: Verify Database Connection

Run this query to test:
```sql
SELECT current_database(), current_user, version();
```

## Step 4: Update Application Properties

Use these connection details in your Spring Boot application:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fraud_detection
spring.datasource.username=fraud_user
spring.datasource.password=fraud_password
```

## Alternative: Use Default PostgreSQL User

If you want to use the default postgres user:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fraud_detection
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password
```

## Test Connection

After running these commands, test your connection with:
```sql
SELECT 'Connection successful!' as status;
```