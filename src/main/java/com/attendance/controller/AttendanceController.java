package com.attendance.controller;

import com.attendance.model.Attendance;
import com.attendance.model.Employee;
import com.attendance.service.AttendanceService;
import com.attendance.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AttendanceController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(EmployeeService employeeService, AttendanceService attendanceService) {
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
    }

    // ===== MARK ATTENDANCE =====

    @GetMapping("/attendance/mark")
    public String showMarkAttendance(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        if (date == null) date = LocalDate.now();
        List<Employee> employees = employeeService.getAllEmployees();

        Map<String, String> statusMap = new HashMap<>();
        Map<String, String> remarkMap = new HashMap<>();
        for (Employee emp : employees) {
            attendanceService.getAttendance(emp.getEmployeeId(), date).ifPresent(a -> {
                statusMap.put(emp.getEmployeeId(), a.getStatus());
                if (a.getRemark() != null) remarkMap.put(emp.getEmployeeId(), a.getRemark());
            });
        }

        model.addAttribute("employees", employees);
        model.addAttribute("selectedDate", date);
        model.addAttribute("statusMap", statusMap);
        model.addAttribute("remarkMap", remarkMap);
        model.addAttribute("activePage", "mark");
        return "mark-attendance";
    }

    @PostMapping("/attendance/mark")
    public String markAttendance(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "employeeId", required = false) List<String> employeeIds,
            @RequestParam(value = "status", required = false) List<String> statuses,
            @RequestParam(value = "remark", required = false) List<String> remarks,
            RedirectAttributes redirectAttributes) {

        if (employeeIds != null && statuses != null && employeeIds.size() == statuses.size()) {
            for (int i = 0; i < employeeIds.size(); i++) {
                String status = statuses.get(i);
                if ("Present".equals(status) || "Absent".equals(status)) {
                    String remark = (remarks != null && i < remarks.size()) ? remarks.get(i) : null;
                    attendanceService.markAttendance(employeeIds.get(i), date, status, remark);
                }
            }
            redirectAttributes.addFlashAttribute("successMessage",
                    "Attendance saved for " + date + " (" + employeeIds.size() + " employees)");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No attendance data received.");
        }
        return "redirect:/attendance/mark?date=" + date;
    }

    // ===== HISTORY =====

    @GetMapping("/attendance/history")
    public String attendanceHistory(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "employeeId", required = false) String employeeId,
            @RequestParam(value = "department", required = false) String department,
            Model model) {

        List<Attendance> records = attendanceService.getFiltered(date, employeeId, department);

        long presentCount = 0, absentCount = 0;
        double percentage = 0.0;
        int totalEmployees = employeeService.getTotalCount();
        if (date != null) {
            presentCount = attendanceService.countPresent(date);
            absentCount = attendanceService.countAbsent(date);
            percentage = attendanceService.getAttendancePercentage(date);
        }

        model.addAttribute("records", records);
        model.addAttribute("allEmployees", employeeService.getAllEmployees());
        model.addAttribute("departments", employeeService.getAllDepartments());
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("presentCount", presentCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("percentage", percentage);
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("activePage", "history");
        return "attendance-history";
    }

    // ===== REPORTS =====

    @GetMapping("/attendance/reports")
    public String reports(
            @RequestParam(value = "month", required = false) String monthStr,
            Model model) {

        YearMonth month;
        if (monthStr != null && !monthStr.isBlank()) {
            month = YearMonth.parse(monthStr);
        } else {
            month = YearMonth.now();
        }

        List<Attendance> monthRecords = attendanceService.getByMonth(month);
        List<Map<String, Object>> rankings = attendanceService.getEmployeeRankings();
        Map<String, Long> deptCounts = employeeService.getDepartmentCounts();

        long monthPresent = monthRecords.stream()
                .filter(a -> "Present".equalsIgnoreCase(a.getStatus())).count();
        long monthAbsent = monthRecords.size() - monthPresent;

        model.addAttribute("month", month);
        model.addAttribute("monthStr", month.toString());
        model.addAttribute("monthRecords", monthRecords);
        model.addAttribute("monthPresent", monthPresent);
        model.addAttribute("monthAbsent", monthAbsent);
        model.addAttribute("rankings", rankings);
        model.addAttribute("deptCounts", deptCounts);
        model.addAttribute("activePage", "reports");
        return "reports";
    }

    // ===== EMPLOYEE HISTORY =====

    @GetMapping("/attendance/employee/{id}")
    public String employeeHistory(@PathVariable("id") String id, Model model,
                                  RedirectAttributes redirectAttributes) {
        var empOpt = employeeService.findById(id);
        if (empOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found: " + id);
            return "redirect:/attendance/history";
        }

        List<Attendance> records = attendanceService.getByEmployee(id);
        long present = records.stream().filter(a -> "Present".equalsIgnoreCase(a.getStatus())).count();
        long absent = records.size() - present;
        double pct = records.isEmpty() ? 0.0 : Math.round((present * 10000.0) / records.size()) / 100.0;

        model.addAttribute("employee", empOpt.get());
        model.addAttribute("records", records);
        model.addAttribute("presentCount", present);
        model.addAttribute("absentCount", absent);
        model.addAttribute("percentage", pct);
        model.addAttribute("activePage", "history");
        return "employee-history";
    }

    // ===== CSV EXPORT =====

    @GetMapping("/attendance/export")
    public ResponseEntity<String> exportCsv(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "employeeId", required = false) String employeeId,
            @RequestParam(value = "department", required = false) String department) {

        List<Attendance> records = attendanceService.getFiltered(date, employeeId, department);
        String csv = attendanceService.toCsv(records);

        String filename = "attendance_export.csv";
        if (date != null) filename = "attendance_" + date + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
