# Employee Attendance Management System (Advanced)

In-memory Spring Boot + Thymeleaf attendance system for college projects.

**No database** – data lives in `ArrayList` (employees) and `HashMap` (attendance).

## New / Advanced Features

- **Dashboard charts** (Chart.js) – Present vs Absent, Department headcount
- **Attendance rankings** – employees ranked by attendance %
- **Reports page** – monthly summary + charts + rankings
- **CSV export** – download filtered attendance records
- **Remarks** – optional note when marking attendance (e.g. “Sick leave”)
- **Department filter** – on employees and history
- **Email & designation** fields on employees
- **7 sample employees** + sample attendance for today & yesterday
- **Dockerfile** ready for Render / Docker deploy

## Tech Stack

- Java 17, Spring Boot 3.2.5
- Thymeleaf, Chart.js
- ArrayList + HashMap (no DB)

## Run Locally

```bash
mvn spring-boot:run
```

Open: http://localhost:8080

## Project Structure

```
model/       Employee, Attendance
service/     EmployeeService (ArrayList), AttendanceService (HashMap)
controller/  Dashboard, Employee, Attendance (+ export + reports)
templates/   dashboard, employees, mark, history, reports, ...
```

## Deploy (Render with Docker)

1. Push project to GitHub (include `Dockerfile`)
2. Render → Web Service → **Docker**
3. Dockerfile Path: leave empty
4. Deploy

## Viva Points

- In-memory only – no MySQL/H2/JPA
- Unique key: `employeeId + "_" + date`
- Re-marking updates existing record
- Delete employee cleans related attendance
- Percentage formula handles zero employees
