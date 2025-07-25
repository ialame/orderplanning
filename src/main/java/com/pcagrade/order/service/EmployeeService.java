// ========== ENGLISH VERSION: EmployeeService.java ==========
// src/main/java/com/pcagrade/order/service/EmployeeService.java

package com.pcagrade.order.service;

import com.pcagrade.order.entity.Employee;
import com.pcagrade.order.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Employee Service - English Version
 * Handles all employee-related business logic
 */
@Service
@Transactional
@Validated
@Slf4j
public class EmployeeService {

    private static final int DEFAULT_WORK_HOURS_PER_DAY = 8;
    private static final int MAX_WORK_HOURS_PER_DAY = 12;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    // ========== CRUD OPERATIONS ==========

    /**
     * Create a new employee
     * @param employee the employee to create
     * @return created employee
     */
    public Employee createEmployee(@Valid @NotNull Employee employee) {
        try {
            log.info("Creating new employee: {} {}", employee.getFirstName(), employee.getLastName());

            // Validate business rules
            validateNewEmployee(employee);

            // Set default values if not provided
            if (employee.getWorkHoursPerDay() == null) {
                employee.setWorkHoursPerDay(DEFAULT_WORK_HOURS_PER_DAY);
            }
            if (employee.getActive() == null) {
                employee.setActive(true);
            }
            if (employee.getCreationDate() == null) {
                employee.setCreationDate(LocalDateTime.now());
            }
            if (employee.getModificationDate() == null) {
                employee.setModificationDate(LocalDateTime.now());
            }

            // Save the employee
            Employee savedEmployee = employeeRepository.save(employee);

            log.info("Employee created successfully with ID: {}", savedEmployee.getId());
            return savedEmployee;

        } catch (Exception e) {
            log.error("Error creating employee: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating employee: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing employee
     * @param employee the employee to update
     * @return updated employee
     */
    public Employee updateEmployee(@Valid @NotNull Employee employee) {
        log.info("Updating employee: {}", employee.getId());

        if (!employeeRepository.existsById(employee.getId())) {
            throw new IllegalArgumentException("Employee not found with ID: " + employee.getId());
        }

        employee.setModificationDate(LocalDateTime.now());
        Employee updatedEmployee = employeeRepository.save(employee);

        log.info("Employee updated successfully: {}", updatedEmployee.getId());
        return updatedEmployee;
    }

    /**
     * Find employee by ID
     * @param id the employee ID
     * @return optional employee
     */
    @Transactional(readOnly = true)
    public Optional<Employee> findById(@NotNull String id) {
        try {
            UUID employeeId = UUID.fromString(id.replace("-", ""));
            return employeeRepository.findById(employeeId);
        } catch (Exception e) {
            log.error("Error finding employee by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Find employee by name
     * @param lastName the last name
     * @return optional employee
     */
    @Transactional(readOnly = true)
    public Optional<Employee> findByLastName(@NotNull String lastName) {
        List<Employee> employees = employeeRepository.findByLastName(lastName);
        return employees.isEmpty() ? Optional.empty() : Optional.of(employees.get(0));
    }

    /**
     * Delete an employee
     * @param id the employee ID
     */
    public void deleteEmployee(@NotNull String id) {
        try {
            log.info("Deleting employee: {}", id);

            Optional<Employee> employee = findById(id);
            if (employee.isPresent()) {
                employeeRepository.delete(employee.get());
                log.info("Employee deleted successfully: {}", id);
            } else {
                throw new IllegalArgumentException("Employee not found with ID: " + id);
            }

        } catch (Exception e) {
            log.error("Error deleting employee: {}", e.getMessage());
            throw new RuntimeException("Error deleting employee: " + e.getMessage(), e);
        }
    }

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Get all active employees
     * @return list of active employees as maps for frontend compatibility
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllActiveEmployees() {
        try {
            log.info("Getting all active employees");

            // Check if employee table exists
            String sqlCheckTable = "SHOW TABLES LIKE 'employee'";
            Query queryCheck = entityManager.createNativeQuery(sqlCheckTable);
            @SuppressWarnings("unchecked")
            List<Object> tables = queryCheck.getResultList();

            if (tables.isEmpty()) {
                log.warn("Employee table does not exist");
                return new ArrayList<>();
            }

            // Query to get real employees
            String sql = """
                SELECT 
                    HEX(e.id) as id,
                    e.first_name,
                    e.last_name,
                    e.email,
                    e.work_hours_per_day,
                    e.active,
                    e.creation_date
                FROM employee e
                WHERE e.active = 1
                ORDER BY e.last_name, e.first_name
            """;

            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> employees = new ArrayList<>();

            log.info("Found {} active employees", results.size());

            for (Object[] row : results) {
                Map<String, Object> employee = new HashMap<>();
                employee.put("id", (String) row[0]);
                employee.put("firstName", (String) row[1]);
                employee.put("lastName", (String) row[2]);
                employee.put("email", (String) row[3]);
                employee.put("workHoursPerDay", row[4] != null ? ((Number) row[4]).intValue() : DEFAULT_WORK_HOURS_PER_DAY);
                employee.put("active", row[5] != null ? (Boolean) row[5] : true);
                employee.put("creationDate", row[6]);

                // Calculated fields
                employee.put("fullName", row[1] + " " + row[2]);
                employee.put("available", true);
                employee.put("currentLoad", 0);

                employees.add(employee);
                log.debug("Employee: {} {}", row[1], row[2]);
            }

            return employees;

        } catch (Exception e) {
            log.error("Error getting active employees: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get employees with planning data for a specific date
     * @param date the target date
     * @return list of employees with planning information
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEmployeesWithPlanningData(String date) {
        try {
            log.info("Getting employees with planning data for date: {}", date);

            String sql = """
                SELECT 
                    HEX(e.id) as id,
                    CONCAT(e.first_name, ' ', e.last_name) as name,
                    e.work_hours_per_day * 60 as max_minutes,
                    COALESCE(SUM(p.duration_minutes), 0) as total_minutes,
                    COUNT(p.id) as task_count,
                    COALESCE(SUM(o.card_count), 0) as card_count
                FROM employee e
                LEFT JOIN planning p ON e.id = p.employee_id AND DATE(p.planned_date) = ?
                LEFT JOIN `order` o ON p.order_id = o.id
                WHERE e.active = 1
                GROUP BY e.id, e.first_name, e.last_name, e.work_hours_per_day
                ORDER BY e.last_name, e.first_name
            """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, date);
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> employeesWithPlanning = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> employee = new HashMap<>();
                employee.put("id", (String) row[0]);
                employee.put("name", (String) row[1]);
                employee.put("maxMinutes", ((Number) row[2]).intValue());
                employee.put("totalMinutes", ((Number) row[3]).intValue());
                employee.put("taskCount", ((Number) row[4]).intValue());
                employee.put("cardCount", ((Number) row[5]).intValue());

                // Calculate status based on workload
                int totalMinutes = ((Number) row[3]).intValue();
                int maxMinutes = ((Number) row[2]).intValue();
                double workloadPercent = (double) totalMinutes / maxMinutes;

                String status;
                if (workloadPercent > 1.0) {
                    status = "overloaded";
                } else if (workloadPercent >= 0.9) {
                    status = "full";
                } else {
                    status = "available";
                }
                employee.put("status", status);

                employeesWithPlanning.add(employee);
            }

            log.info("Found {} employees with planning data", employeesWithPlanning.size());
            return employeesWithPlanning;

        } catch (Exception e) {
            log.error("Error getting employees with planning data: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get employee statistics
     * @return map with employee statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Basic counts
            long totalEmployees = employeeRepository.count();
            long activeEmployees = employeeRepository.countByActive(true);
            long inactiveEmployees = totalEmployees - activeEmployees;

            stats.put("totalEmployees", totalEmployees);
            stats.put("activeEmployees", activeEmployees);
            stats.put("inactiveEmployees", inactiveEmployees);

            // Calculate average work hours
            if (activeEmployees > 0) {
                String avgHoursSql = "SELECT AVG(work_hours_per_day) FROM employee WHERE active = 1";
                Query avgQuery = entityManager.createNativeQuery(avgHoursSql);
                Number avgHours = (Number) avgQuery.getSingleResult();
                stats.put("averageWorkHours", avgHours != null ? avgHours.doubleValue() : 0.0);
            } else {
                stats.put("averageWorkHours", 0.0);
            }

            stats.put("success", true);
            stats.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error calculating employee statistics", e);
            stats.put("success", false);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    // ========== VALIDATION METHODS ==========

    /**
     * Validate new employee data
     * @param employee the employee to validate
     */
    private void validateNewEmployee(Employee employee) {
        if (employee.getWorkHoursPerDay() != null &&
                (employee.getWorkHoursPerDay() <= 0 || employee.getWorkHoursPerDay() > MAX_WORK_HOURS_PER_DAY)) {
            throw new IllegalArgumentException(
                    String.format("Work hours per day must be between 1 and %d", MAX_WORK_HOURS_PER_DAY)
            );
        }

        // Check for duplicate email if provided
        if (employee.getEmail() != null && !employee.getEmail().isEmpty()) {
            Optional<Employee> existingEmployee = employeeRepository.findByEmail(employee.getEmail());
            if (existingEmployee.isPresent()) {
                throw new IllegalArgumentException("Employee with email already exists: " + employee.getEmail());
            }
        }
    }

    // ========== LEGACY COMPATIBILITY METHODS ==========

    /**
     * Legacy method for compatibility with existing code
     * @deprecated Use getAllActiveEmployees() instead
     */
    @Deprecated
    public List<Map<String, Object>> getTousEmployesActifs() {
        log.warn("Using deprecated method getTousEmployesActifs(), please use getAllActiveEmployees()");
        return getAllActiveEmployees();
    }

    /**
     * Legacy method for compatibility with existing code
     * @deprecated Use createEmployee() instead
     */
    @Deprecated
    public Employee creerEmploye(Employee employee) {
        log.warn("Using deprecated method creerEmploye(), please use createEmployee()");
        return createEmployee(employee);
    }
}