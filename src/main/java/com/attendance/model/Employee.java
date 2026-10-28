package com.attendance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Employee model – stored in ArrayList (in-memory).
 */
public class Employee {

    @NotBlank(message = "Employee ID is required")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    private String employeeId;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Phone number is required")
    @Size(min = 7, max = 15, message = "Phone must be between 7 and 15 characters")
    private String phone;

    private String email;
    private String designation;

    public Employee() {
    }

    public Employee(String employeeId, String fullName, String department, String phone) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.department = department;
        this.phone = phone;
    }

    public Employee(String employeeId, String fullName, String department,
                    String phone, String email, String designation) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.department = department;
        this.phone = phone;
        this.email = email;
        this.designation = designation;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    @Override
    public String toString() {
        return "Employee{id='" + employeeId + "', name='" + fullName +
                "', dept='" + department + "'}";
    }
}
