# RevWorkforce - Human Resource Management System

RevWorkforce is a full-stack HR Management System built with Spring Boot, Java 21, and Thymeleaf. It provides comprehensive features for managing employees, leave balances, performance reviews, goals, and organizational structures.

## Core Features
1. **User Authentication & Authorization**: Secure login and registration with JWT + BCrypt. Role-based access control (Admin, Manager, Employee).
2. **Employee Management**: CRUD operations for employees, departments, and designations.
3. **Leave Management**: Apply for leaves, view balances, manager approvals, and holiday calendar.
4. **Performance & Goals**: Create custom performance goals, write reviews, and receive manager feedback.
5. **Admin Controls**: System-wide announcements, audit logging, and bulk employee management.

## Technology Stack
- **Backend**: Java 21, Spring Boot 3.3.5
- **Security**: Spring Security 6, JWT (JSON Web Tokens), BCrypt Password Hashing
- **Database**: Oracle DB (ojdbc11), Spring Data JPA, Hibernate
- **Frontend**: Thymeleaf, HTML5, Vanilla CSS (Inter font, fully responsive)
- **Validation**: Jakarta Validation API

## Prerequisites
- Java Development Kit (JDK) 21
- Apache Maven
- Oracle Database (running locally on port 1521, or update `application.properties`)

## Database Configuration
Ensure you have an Oracle Database instance running with the following credentials (or update `src/main/resources/application.properties`):
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=hrm
spring.datasource.password=hrm123
```

## Running the Application
1. **Build the project:**
   ```bash
   mvn clean package -DskipTests
   ```7
2. **Run the Spring Boot app:**
   ```bash
   mvn spring-boot:run
   ```
   The application will automatically start on `http://localhost:9091`.
3. **Run Unit Tests:**
   ```bash
   mvn test
   ```

## Initial Data Setup
On the first run, the `DataInitializer` will automatically seed the database with departments, designations, and default users.

### Demo Credentials
- **Admin**: `admin@revworkforce.com` / `Admin@123`
- **Manager**: `manager@revworkforce.com` / `Manager@123`
- **Employee**: `employee@revworkforce.com` / `Employee@123`

## Entity Relationship Diagram (ERD) Structure
The database follows a normalized relational structure:
- **EMPLOYEE**: Core user table with self-referencing `manager_id`, and foreign keys to `DEPARTMENT` and `DESIGNATION`.
- **DEPARTMENT** & **DESIGNATION**: Lookup tables for organizational structure.
- **LEAVE_BALANCE**: 1-to-1 relationship with `EMPLOYEE` tracking available leave days.
- **LEAVE_APPLICATION**: Many-to-1 with `EMPLOYEE`, tracking individual leave requests.
- **PERFORMANCE_REVIEW** & **GOAL**: Many-to-1 with `EMPLOYEE`, storing performance tracking data.
- **ANNOUNCEMENT** & **AUDIT_LOG**: System-wide event tracking and communications.
- **NOTIFICATION**: Many-to-1 with `EMPLOYEE` for alert history.

## Architecture Guidelines
- **Three-Layer Architecture**: Controllers (REST & Web) -> Services (Interface-based) -> Repositories
- **Security**: Stateless JWT authentication for API access; cookie-based JWT for Thymeleaf page rendering.
- **Error Handling**: Standardized JSON responses via `GlobalExceptionHandler`.
