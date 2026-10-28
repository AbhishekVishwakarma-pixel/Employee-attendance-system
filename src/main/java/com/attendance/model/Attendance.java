package com.attendance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance record for one employee on one date.
 * Stored in HashMap (key = employeeId + "_" + date).
 */
public class Attendance {

    private String employeeId;
    private String employeeName;
    private String department;
    private LocalDate date;
    private String status;          // Present / Absent
    private String remark;          // optional note
    private LocalDateTime markedAt; // when it was last saved

    public Attendance() {
    }

    public Attendance(String employeeId, String employeeName, String department,
                      LocalDate date, String status, String remark) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.date = date;
        this.status = status;
        this.remark = remark;
        this.markedAt = LocalDateTime.now();
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }

    public String getKey() {
        return employeeId + "_" + date;
    }

    @Override
    public String toString() {
        return "Attendance{empId='" + employeeId + "', name='" + employeeName +
                "', date=" + date + ", status='" + status + "'}";
    }
}
