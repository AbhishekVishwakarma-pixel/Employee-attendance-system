package com.attendance.service;

import com.attendance.model.Attendance;
import com.attendance.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory attendance storage using HashMap.
 * Key = employeeId + "_" + date
 */
@Service
public class AttendanceService {

    private final Map<String, Attendance> attendanceMap = new HashMap<>();
    private final EmployeeService employeeService;

    @Autowired
    public AttendanceService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    private String buildKey(String employeeId, LocalDate date) {
        return employeeId.toUpperCase() + "_" + date;
    }

    public void markAttendance(String employeeId, LocalDate date, String status, String remark) {
        Optional<Employee> empOpt = employeeService.findById(employeeId);
        if (empOpt.isEmpty()) return;
        Employee emp = empOpt.get();
        String key = buildKey(employeeId, date);
        Attendance record = new Attendance(
                emp.getEmployeeId(), emp.getFullName(), emp.getDepartment(),
                date, status, remark);
        record.setMarkedAt(LocalDateTime.now());
        attendanceMap.put(key, record);
    }

    public Optional<Attendance> getAttendance(String employeeId, LocalDate date) {
        return Optional.ofNullable(attendanceMap.get(buildKey(employeeId, date)));
    }

    public List<Attendance> getByDate(LocalDate date) {
        return attendanceMap.values().stream()
                .filter(a -> a.getDate().equals(date))
                .sorted(Comparator.comparing(Attendance::getEmployeeId))
                .collect(Collectors.toList());
    }

    public List<Attendance> getByEmployee(String employeeId) {
        return attendanceMap.values().stream()
                .filter(a -> a.getEmployeeId().equalsIgnoreCase(employeeId))
                .sorted(Comparator.comparing(Attendance::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Attendance> getFiltered(LocalDate date, String employeeId, String department) {
        return attendanceMap.values().stream()
                .filter(a -> date == null || a.getDate().equals(date))
                .filter(a -> employeeId == null || employeeId.isBlank()
                        || a.getEmployeeId().equalsIgnoreCase(employeeId.trim()))
                .filter(a -> department == null || department.isBlank()
                        || (a.getDepartment() != null && a.getDepartment().equalsIgnoreCase(department.trim())))
                .sorted(Comparator.comparing(Attendance::getDate).reversed()
                        .thenComparing(Attendance::getEmployeeId))
                .collect(Collectors.toList());
    }

    public List<Attendance> getByDateRange(LocalDate from, LocalDate to) {
        return attendanceMap.values().stream()
                .filter(a -> !a.getDate().isBefore(from) && !a.getDate().isAfter(to))
                .sorted(Comparator.comparing(Attendance::getDate).reversed()
                        .thenComparing(Attendance::getEmployeeId))
                .collect(Collectors.toList());
    }

    public List<Attendance> getByMonth(YearMonth month) {
        return attendanceMap.values().stream()
                .filter(a -> YearMonth.from(a.getDate()).equals(month))
                .sorted(Comparator.comparing(Attendance::getDate)
                        .thenComparing(Attendance::getEmployeeId))
                .collect(Collectors.toList());
    }

    public List<Attendance> getRecent(int limit) {
        return attendanceMap.values().stream()
                .sorted(Comparator.comparing(Attendance::getDate).reversed()
                        .thenComparing(Attendance::getEmployeeId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public long countPresent(LocalDate date) {
        return getByDate(date).stream()
                .filter(a -> "Present".equalsIgnoreCase(a.getStatus())).count();
    }

    public long countAbsent(LocalDate date) {
        return getByDate(date).stream()
                .filter(a -> "Absent".equalsIgnoreCase(a.getStatus())).count();
    }

    public double getAttendancePercentage(LocalDate date) {
        int total = employeeService.getTotalCount();
        if (total == 0) return 0.0;
        long present = countPresent(date);
        return Math.round((present * 10000.0) / total) / 100.0;
    }

    public Map<String, Long> getPresentByDepartment(LocalDate date) {
        return getByDate(date).stream()
                .filter(a -> "Present".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.groupingBy(
                        a -> a.getDepartment() != null ? a.getDepartment() : "Unknown",
                        Collectors.counting()));
    }

    public List<Map<String, Object>> getEmployeeRankings() {
        Map<String, List<Attendance>> byEmp = attendanceMap.values().stream()
                .collect(Collectors.groupingBy(Attendance::getEmployeeId));

        List<Map<String, Object>> rankings = new ArrayList<>();
        for (Map.Entry<String, List<Attendance>> entry : byEmp.entrySet()) {
            List<Attendance> list = entry.getValue();
            long present = list.stream().filter(a -> "Present".equalsIgnoreCase(a.getStatus())).count();
            double pct = list.isEmpty() ? 0 : Math.round((present * 10000.0) / list.size()) / 100.0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("employeeId", entry.getKey());
            row.put("employeeName", list.get(0).getEmployeeName());
            row.put("department", list.get(0).getDepartment());
            row.put("totalDays", list.size());
            row.put("presentDays", present);
            row.put("absentDays", list.size() - present);
            row.put("percentage", pct);
            rankings.add(row);
        }
        rankings.sort((a, b) -> Double.compare((Double) b.get("percentage"), (Double) a.get("percentage")));
        return rankings;
    }

    public void removeAllForEmployee(String employeeId) {
        attendanceMap.entrySet().removeIf(e ->
                e.getValue().getEmployeeId().equalsIgnoreCase(employeeId));
    }

    public void updateEmployeeInfo(String employeeId, String newName, String newDept) {
        attendanceMap.values().stream()
                .filter(a -> a.getEmployeeId().equalsIgnoreCase(employeeId))
                .forEach(a -> {
                    a.setEmployeeName(newName);
                    a.setDepartment(newDept);
                });
    }

    public int getTotalRecords() {
        return attendanceMap.size();
    }

    public String toCsv(List<Attendance> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("Employee ID,Name,Department,Date,Status,Remark,Marked At\n");
        for (Attendance a : records) {
            sb.append(csv(a.getEmployeeId())).append(',')
              .append(csv(a.getEmployeeName())).append(',')
              .append(csv(a.getDepartment())).append(',')
              .append(a.getDate()).append(',')
              .append(csv(a.getStatus())).append(',')
              .append(csv(a.getRemark())).append(',')
              .append(a.getMarkedAt() != null ? a.getMarkedAt() : "").append('\n');
        }
        return sb.toString();
    }

    private String csv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
