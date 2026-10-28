package com.attendance.service;

import com.attendance.model.Employee;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory employee storage using ArrayList.
 */
@Service
public class EmployeeService {

    private final List<Employee> employees = new ArrayList<>();

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    public Optional<Employee> findById(String employeeId) {
        if (employeeId == null) return Optional.empty();
        return employees.stream()
                .filter(e -> e.getEmployeeId().equalsIgnoreCase(employeeId.trim()))
                .findFirst();
    }

    public boolean existsById(String employeeId) {
        return findById(employeeId).isPresent();
    }

    public boolean addEmployee(Employee employee) {
        if (existsById(employee.getEmployeeId())) return false;
        employee.setEmployeeId(employee.getEmployeeId().trim().toUpperCase());
        employees.add(employee);
        return true;
    }

    public boolean updateEmployee(Employee updated) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmployeeId().equalsIgnoreCase(updated.getEmployeeId())) {
                updated.setEmployeeId(employees.get(i).getEmployeeId());
                employees.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean deleteEmployee(String employeeId) {
        return employees.removeIf(e -> e.getEmployeeId().equalsIgnoreCase(employeeId));
    }

    public List<Employee> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllEmployees();
        String key = keyword.trim().toLowerCase();
        return employees.stream()
                .filter(e -> e.getEmployeeId().toLowerCase().contains(key)
                        || e.getFullName().toLowerCase().contains(key)
                        || (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(key))
                        || (e.getDesignation() != null && e.getDesignation().toLowerCase().contains(key)))
                .collect(Collectors.toList());
    }

    public List<Employee> getByDepartment(String department) {
        if (department == null || department.isBlank()) return getAllEmployees();
        return employees.stream()
                .filter(e -> e.getDepartment().equalsIgnoreCase(department.trim()))
                .collect(Collectors.toList());
    }

    public List<String> getAllDepartments() {
        return employees.stream()
                .map(Employee::getDepartment)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public int getTotalCount() {
        return employees.size();
    }

    public Map<String, Long> getDepartmentCounts() {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
    }
}
