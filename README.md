# Employee Leave Management System
Spring Boot | MySQL | Spring Security | JPA | JWT

---

## Tech Stack
| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2 |
| Database | MySQL 8 |
| Security | Spring Security + BCrypt + JWT |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| Java | 17 |

---

## Project Structure
```
src/main/java/com/leave/management/
├── LeaveManagementApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── LeaveController.java
│   ├── ManagerController.java
│   └── AdminController.java
├── dto/
│   ├── ApiResponse.java
│   ├── AuthDTOs.java
│   └── LeaveDTOs.java
├── entity/
│   ├── User.java
│   ├── LeaveApplication.java
│   └── LeaveBalance.java
├── enums/
│   ├── Role.java           (ADMIN, MANAGER, EMPLOYEE)
│   ├── LeaveType.java      (ANNUAL, SICK, CASUAL, MATERNITY, PATERNITY, UNPAID, COMPENSATORY)
│   └── LeaveStatus.java    (PENDING, APPROVED, REJECTED, CANCELLED)
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── LeaveException.java
├── repository/
│   ├── UserRepository.java
│   ├── LeaveApplicationRepository.java
│   └── LeaveBalanceRepository.java
├── security/
│   ├── JwtUtils.java
│   └── JwtAuthFilter.java
└── service/
    ├── AuthService.java
    ├── LeaveService.java
    └── impl/
        ├── AuthServiceImpl.java
        ├── LeaveServiceImpl.java
        └── UserDetailsServiceImpl.java
```

---

## Setup Instructions

### 1. Database
```sql
CREATE DATABASE leave_management_db;
```

### 2. Configure application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/leave_management_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
app.jwt.secret=YOUR_256BIT_HEX_SECRET
```

### 3. Run
```bash
mvn clean install
mvn spring-boot:run
```

Server starts at: `http://localhost:8080`

---

## API Endpoints

### Auth (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login & get JWT token |

### Employee (ROLE_EMPLOYEE)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/leaves/apply` | Apply for leave |
| GET | `/api/leaves/my` | View my leave history |
| GET | `/api/leaves/{id}` | View specific leave |
| PATCH | `/api/leaves/{id}/cancel` | Cancel leave |
| GET | `/api/leaves/balance` | View my leave balance |

### Manager / Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/manager/leaves/pending` | View pending applications |
| GET | `/api/manager/leaves` | View all (filter by status) |
| GET | `/api/manager/leaves/department/{dept}` | View by department |
| PUT | `/api/manager/leaves/{id}/review` | Approve or Reject |
| GET | `/api/manager/employees/{id}/balance` | View employee balance |

### Admin Only
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| GET | `/api/admin/users/{id}` | Get user by ID |
| PATCH | `/api/admin/users/{id}/activate` | Activate user |
| PATCH | `/api/admin/users/{id}/deactivate` | Deactivate user |

---

## Authentication

All protected endpoints require:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## Example Requests

### Register
```json
POST /api/auth/register
{
  "fullName": "John Doe",
  "email": "john@company.com",
  "password": "secure123",
  "role": "ROLE_EMPLOYEE",
  "department": "Engineering"
}
```

### Apply for Leave
```json
POST /api/leaves/apply
Authorization: Bearer <token>
{
  "leaveType": "ANNUAL",
  "startDate": "2025-04-01",
  "endDate": "2025-04-05",
  "reason": "Family vacation"
}
```

### Review Leave (Manager)
```json
PUT /api/manager/leaves/1/review
Authorization: Bearer <token>
{
  "status": "APPROVED",
  "reviewComments": "Approved. Enjoy your leave!"
}
```

---

## Business Rules
- Leave dates cannot be in the past
- Overlapping approved leaves are rejected
- Total days calculated as working days only (Mon–Fri)
- Leave balance is auto-deducted on approval
- Balance is restored on cancellation of an approved leave
- Unpaid leave has no balance limit
- Default balances: Annual=18, Sick=12, Casual=6, Maternity=180, Paternity=15




<img width="1280" height="746" alt="Screenshot 2026-03-03 at 6 56 39 AM" src="https://github.com/user-attachments/assets/37925d98-c336-49ea-b62a-983aebf9fa45" />



<img width="1280" height="746" alt="Screenshot 2026-03-03 at 6 58 40 AM" src="https://github.com/user-attachments/assets/3c0969b9-eb7e-41f2-9006-ce2824d36e2b" />



<img width="1280" height="746" alt="Screenshot 2026-03-03 at 6 59 02 AM" src="https://github.com/user-attachments/assets/e7e10b90-a831-41ef-b565-80295c532e9d" />

<img width="1280" height="747" alt="Screenshot 2026-03-03 at 6 59 18 AM" src="https://github.com/user-attachments/assets/3334c428-faf2-4a86-9e66-c93c6b432a48" />



