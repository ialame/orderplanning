package com.pcagrade.order.service;

import com.pcagrade.order.entity.Employee;
import com.pcagrade.order.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing employees and employee-related operations
 * Translated from EmployeService to EmployeeService with enhanced functionality
 */
@Service
@Transactional
@Validated
@Slf4j
public class EmployeeService {

    private static final double DEFAULT_WORK_HOURS = 8.0;
    private static final double MIN_WORK_HOURS = 0.5;
    private static final double MAX_WORK_HOURS = 12.0;

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
        log.info("Creating new employee: {} {}", employee.getFirstName(), employee.getLastName());

        // Validate business rules
        validateNewEmployee(employee);

        // Set default values
        if (employee.getWorkHoursPerDay() == null) {
            employee.setWorkHoursPerDay(DEFAULT_WORK_HOURS);
        }
        if (employee.getActive() == null) {
            employee.setActive(true);  // FIXED: Use setActive instead of getActive() = true
        }
        if (employee.getSkillLevel() == null) {
            employee.setSkillLevel(Employee.SkillLevel.INTERMEDIATE);
        }

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", savedEmployee.getId());
        return savedEmployee;
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

        // Validate email uniqueness (excluding current employee)
        if (!employeeRepository.isEmailAvailable(employee.getEmail(), employee.getId())) {
            throw new IllegalArgumentException("Email already exists: " + employee.getEmail());
        }

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee updated successfully: {}", employee.getId());
        return savedEmployee;
    }

    /**
     * Get employee by ID
     * @param id the employee ID
     * @return employee if found
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(@NotNull UUID id) {
        return employeeRepository.findById(id);
    }

    /**
     * Get all employees with pagination
     * @param pageable pagination parameters
     * @return page of employees
     */
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Delete employee by ID
     * @param id the employee ID
     */
    public void deleteEmployee(@NotNull UUID id) {
        log.info("Deleting employee: {}", id);

        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found: " + id);
        }

        employeeRepository.deleteById(id);
        log.info("Employee deleted successfully: {}", id);
    }

    // ========== BUSINESS OPERATIONS ==========

    /**
     * Find all active employees
     * @return list of active employees
     */
    @Transactional(readOnly = true)
    public List<Employee> findActiveEmployees() {
        return employeeRepository.findByActiveTrue();
    }

    /**
     * Find employees by email
     * @param email the email to search
     * @return employee if found
     */
    @Transactional(readOnly = true)
    public Optional<Employee> findByEmail(@Email @NotBlank String email) {
        return employeeRepository.findByEmail(email);
    }

    /**
     * Search employees by name pattern
     * @param namePattern pattern to search
     * @return list of matching employees
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesByName(@NotBlank String namePattern) {
        String searchPattern = "%" + namePattern.toLowerCase() + "%";
        return employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                namePattern, namePattern);
    }

    /**
     * Get employee workload distribution
     * @return map of employee workload statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeWorkloadDistribution() {
        List<Employee> activeEmployees = findActiveEmployees();
        Map<String, Object> distribution = new HashMap<>();

        if (activeEmployees.isEmpty()) {
            distribution.put("totalEmployees", 0);
            distribution.put("averageWorkHours", 0.0);
            distribution.put("employees", Collections.emptyList());
        } else {
            double totalHours = activeEmployees.stream()
                    .mapToDouble(Employee::getWorkHoursPerDay)
                    .sum();
            double averageHours = totalHours / activeEmployees.size();

            distribution.put("totalEmployees", activeEmployees.size());
            distribution.put("averageWorkHours", Math.round(averageHours * 100.0) / 100.0);
            distribution.put("totalWorkHours", Math.round(totalHours * 100.0) / 100.0);

            List<Map<String, Object>> employeeData = activeEmployees.stream()
                    .map(this::convertEmployeeToWorkloadMap)
                    .collect(Collectors.toList());
            distribution.put("employees", employeeData);
        }

        return distribution;
    }

    // ========== EMPLOYEE ACTIVATION/DEACTIVATION ==========

    /**
     * Activate an employee
     * @param id the employee ID
     * @return activated employee
     */
    public Employee activateEmployee(@NotNull UUID id) {
        log.info("Activating employee: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

        employee.setActive(true);
        employee.setTerminationDate(null); // Clear termination date

        Employee activatedEmployee = employeeRepository.save(employee);
        log.info("Employee activated successfully: {}", id);
        return activatedEmployee;
    }

    /**
     * Deactivate an employee
     * @param id the employee ID
     * @return deactivated employee
     */
    public Employee deactivateEmployee(@NotNull UUID id) {
        log.info("Deactivating employee: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

        employee.setActive(false);
        employee.setTerminationDate(LocalDateTime.now());

        Employee deactivatedEmployee = employeeRepository.save(employee);
        log.info("Employee deactivated successfully: {}", id);
        return deactivatedEmployee;
    }

    /**
     * Find inactive employees
     * @return list of inactive employees
     */
    @Transactional(readOnly = true)
    public List<Employee> findInactiveEmployees() {
        return employeeRepository.findByActiveFalse();
    }

    // ========== UTILITY METHODS ==========

    /**
     * Check if email is available
     * @param email the email to check
     * @param excludeEmployeeId employee ID to exclude from check
     * @return true if available
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(@Email @NotBlank String email, UUID excludeEmployeeId) {
        return employeeRepository.isEmailAvailable(email, excludeEmployeeId);
    }

    /**
     * Convert employee to workload map
     * @param employee the employee
     * @return workload map
     */
    private Map<String, Object> convertEmployeeToWorkloadMap(Employee employee) {
        Map<String, Object> workload = new HashMap<>();
        workload.put("id", employee.getId());
        workload.put("name", employee.getFirstName() + " " + employee.getLastName());
        workload.put("email", employee.getEmail());
        workload.put("workHoursPerDay", employee.getWorkHoursPerDay());
        workload.put("workMinutesPerDay", (int) (employee.getWorkHoursPerDay() * 60));
        workload.put("active", employee.getActive());
        workload.put("skillLevel", employee.getSkillLevel());
        workload.put("department", employee.getDepartment());
        workload.put("position", employee.getPosition());
        return workload;
    }

    /**
     * Validate new employee business rules
     * @param employee the employee to validate
     */
    private void validateNewEmployee(Employee employee) {
        // Check email uniqueness
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + employee.getEmail());
        }

        // Validate work hours
        if (employee.getWorkHoursPerDay() != null) {
            if (employee.getWorkHoursPerDay() < MIN_WORK_HOURS ||
                    employee.getWorkHoursPerDay() > MAX_WORK_HOURS) {
                throw new IllegalArgumentException(
                        String.format("Work hours must be between %.1f and %.1f",
                                MIN_WORK_HOURS, MAX_WORK_HOURS));
            }
        }
    }

    /**
     * Get work capacity category based on hours
     * @param workHours daily work hours
     * @return capacity category
     */
    private String getWorkCapacityCategory(double workHours) {
        if (workHours < 4.0) return "LOW";
        else if (workHours < 7.0) return "MEDIUM";
        else if (workHours <= 8.0) return "STANDARD";
        else return "HIGH";
    }

    /**
     * Count active employees
     * @return number of active employees
     */
    @Transactional(readOnly = true)
    public long countActiveEmployees() {
        return employeeRepository.countByActiveTrue();
    }

    /**
     * Get active employees as simple map data
     * @return list of employee data maps
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveEmployeesData() {
        try {
            String sql = """
                SELECT 
                    HEX(e.id) as id,
                    e.first_name as firstName,
                    e.last_name as lastName,
                    e.email as email,
                    e.work_hours_per_day as workHoursPerDay,
                    e.active as active,
                    e.department as department,
                    e.position as position,
                    e.skill_level as skillLevel,
                    e.creation_date as creationDate
                FROM j_employee e
                WHERE e.active = true
                ORDER BY e.last_name, e.first_name
                """;

            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> employees = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", (String) row[0]);
                employeeData.put("firstName", (String) row[1]);
                employeeData.put("lastName", (String) row[2]);
                employeeData.put("email", (String) row[3]);
                employeeData.put("workHoursPerDay", row[4]);
                employeeData.put("active", row[5]);
                employeeData.put("department", (String) row[6]);
                employeeData.put("position", (String) row[7]);
                employeeData.put("skillLevel", (String) row[8]);
                employeeData.put("creationDate", row[9]);

                // Add calculated fields
                Double workHours = (Double) row[4];
                if (workHours != null) {
                    employeeData.put("workMinutesPerDay", (int) (workHours * 60));
                    employeeData.put("workCapacity", getWorkCapacityCategory(workHours));
                    employeeData.put("isPartTime", workHours < 8.0);
                    employeeData.put("isFullTime", workHours >= 8.0);
                }

                // Add full name
                String firstName = (String) row[1];
                String lastName = (String) row[2];
                employeeData.put("fullName", firstName + " " + lastName);
                employeeData.put("displayName", lastName + ", " + firstName);

                employees.add(employeeData);
            }

            log.info("Found {} active employees", employees.size());
            return employees;

        } catch (Exception e) {
            log.error("Error fetching active employees: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}