package com.attendance.controller;

import com.attendance.model.Employee;
import com.attendance.service.AttendanceService;
import com.attendance.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, AttendanceService attendanceService) {
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public String listEmployees(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "department", required = false) String department,
            Model model) {

        List<Employee> employees;
        if (keyword != null && !keyword.trim().isEmpty()) {
            employees = employeeService.search(keyword);
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchPerformed", true);
        } else if (department != null && !department.isBlank()) {
            employees = employeeService.getByDepartment(department);
            model.addAttribute("selectedDepartment", department);
            model.addAttribute("searchPerformed", true);
        } else {
            employees = employeeService.getAllEmployees();
            model.addAttribute("searchPerformed", false);
        }

        model.addAttribute("employees", employees);
        model.addAttribute("departments", employeeService.getAllDepartments());
        model.addAttribute("activePage", "employees");
        return "employees";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Add Employee");
        model.addAttribute("activePage", "employees");
        return "employee-form";
    }

    @PostMapping("/save")
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        boolean isNew = !employeeService.existsById(employee.getEmployeeId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", !isNew);
            model.addAttribute("pageTitle", isNew ? "Add Employee" : "Edit Employee");
            model.addAttribute("activePage", "employees");
            return "employee-form";
        }

        if (isNew) {
            boolean added = employeeService.addEmployee(employee);
            if (!added) {
                bindingResult.rejectValue("employeeId", "duplicate", "Employee ID already exists");
                model.addAttribute("isEdit", false);
                model.addAttribute("pageTitle", "Add Employee");
                model.addAttribute("activePage", "employees");
                return "employee-form";
            }
            redirectAttributes.addFlashAttribute("successMessage", "Employee added successfully!");
        } else {
            employeeService.updateEmployee(employee);
            attendanceService.updateEmployeeInfo(
                    employee.getEmployeeId(), employee.getFullName(), employee.getDepartment());
            redirectAttributes.addFlashAttribute("successMessage", "Employee updated successfully!");
        }
        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<Employee> opt = employeeService.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found: " + id);
            return "redirect:/employees";
        }
        model.addAttribute("employee", opt.get());
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Edit Employee");
        model.addAttribute("activePage", "employees");
        return "employee-form";
    }

    @GetMapping("/delete/{id}")
    public String showDeleteConfirm(@PathVariable("id") String id, Model model,
                                    RedirectAttributes redirectAttributes) {
        Optional<Employee> opt = employeeService.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found: " + id);
            return "redirect:/employees";
        }
        model.addAttribute("employee", opt.get());
        model.addAttribute("activePage", "employees");
        return "employee-delete";
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable("id") String id,
                                 RedirectAttributes redirectAttributes) {
        boolean deleted = employeeService.deleteEmployee(id);
        if (deleted) {
            attendanceService.removeAllForEmployee(id);
            redirectAttributes.addFlashAttribute("successMessage", "Employee and related attendance deleted!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found.");
        }
        return "redirect:/employees";
    }
}
