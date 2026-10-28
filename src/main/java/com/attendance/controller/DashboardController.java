package com.attendance.controller;

import com.attendance.model.Attendance;
import com.attendance.service.AttendanceService;
import com.attendance.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Dashboard with stats, charts data, rankings and recent records.
 */
@Controller
public class DashboardController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;

    @Autowired
    public DashboardController(EmployeeService employeeService, AttendanceService attendanceService) {
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();

        int totalEmployees = employeeService.getTotalCount();
        long presentToday = attendanceService.countPresent(today);
        long absentToday = attendanceService.countAbsent(today);
        double percentage = attendanceService.getAttendancePercentage(today);
        int totalRecords = attendanceService.getTotalRecords();
        List<Attendance> recent = attendanceService.getRecent(10);
        Map<String, Long> deptCounts = employeeService.getDepartmentCounts();
        Map<String, Long> presentByDept = attendanceService.getPresentByDepartment(today);
        List<Map<String, Object>> rankings = attendanceService.getEmployeeRankings();

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("presentToday", presentToday);
        model.addAttribute("absentToday", absentToday);
        model.addAttribute("percentage", percentage);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("today", today);
        model.addAttribute("recentRecords", recent);
        model.addAttribute("deptCounts", deptCounts);
        model.addAttribute("presentByDept", presentByDept);
        model.addAttribute("rankings", rankings);
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }
}
