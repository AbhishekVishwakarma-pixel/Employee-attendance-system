package com.attendance;

import com.attendance.model.Employee;
import com.attendance.service.AttendanceService;
import com.attendance.service.EmployeeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class AttendanceManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceManagementSystemApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  Employee Attendance System is running!");
        System.out.println("  Open: http://localhost:8080");
        System.out.println("=================================================");
    }

    @Bean
    public CommandLineRunner loadSampleData(EmployeeService employeeService,
                                            AttendanceService attendanceService) {
        return args -> {
            if (!employeeService.getAllEmployees().isEmpty()) return;

            employeeService.addEmployee(new Employee("EMP001", "Rahul Sharma", "IT",
                    "9876543210", "rahul@company.com", "Software Engineer"));
            employeeService.addEmployee(new Employee("EMP002", "Priya Patel", "HR",
                    "9123456780", "priya@company.com", "HR Manager"));
            employeeService.addEmployee(new Employee("EMP003", "Amit Kumar", "Finance",
                    "9988776655", "amit@company.com", "Accountant"));
            employeeService.addEmployee(new Employee("EMP004", "Sneha Reddy", "Marketing",
                    "9012345678", "sneha@company.com", "Marketing Executive"));
            employeeService.addEmployee(new Employee("EMP005", "Vikram Singh", "Operations",
                    "9876501234", "vikram@company.com", "Ops Lead"));
            employeeService.addEmployee(new Employee("EMP006", "Ananya Iyer", "IT",
                    "9555123456", "ananya@company.com", "QA Engineer"));
            employeeService.addEmployee(new Employee("EMP007", "Rohan Mehta", "Sales",
                    "9444987654", "rohan@company.com", "Sales Executive"));

            // Sample attendance for today and yesterday so dashboard looks populated
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            attendanceService.markAttendance("EMP001", today, "Present", null);
            attendanceService.markAttendance("EMP002", today, "Present", null);
            attendanceService.markAttendance("EMP003", today, "Absent", "On leave");
            attendanceService.markAttendance("EMP004", today, "Present", null);
            attendanceService.markAttendance("EMP005", today, "Present", null);
            attendanceService.markAttendance("EMP006", today, "Absent", "Sick leave");
            attendanceService.markAttendance("EMP007", today, "Present", null);

            attendanceService.markAttendance("EMP001", yesterday, "Present", null);
            attendanceService.markAttendance("EMP002", yesterday, "Absent", "Personal work");
            attendanceService.markAttendance("EMP003", yesterday, "Present", null);
            attendanceService.markAttendance("EMP004", yesterday, "Present", null);
            attendanceService.markAttendance("EMP005", yesterday, "Present", null);
            attendanceService.markAttendance("EMP006", yesterday, "Present", null);
            attendanceService.markAttendance("EMP007", yesterday, "Absent", null);

            System.out.println("Sample employees + attendance loaded.");
        };
    }
}
