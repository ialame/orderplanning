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
            employee.getActive() = true;
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
            throw new IllegalArgumentException("Email already in use: " + employee.getEmail());
        }

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
    public Optional<Employee> findById(@NotNull UUID id) {
        return employeeRepository.findById(id);
    }

    /**
     * Find employee by email
     * @param email the email address
     * @return optional employee
     */
    @Transactional(readOnly = true)
    public Optional<Employee> findByEmail(@NotNull @Email String email) {
        return employeeRepository.findByEmail(email.toLowerCase().trim());
    }

    /**
     * Get all employees with pagination
     * @param pageable pagination information
     * @return page of employees
     */
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    /**
     * Delete an employee (soft delete by setting inactive)
     * @param id the employee ID
     */
    public void deleteEmployee(@NotNull UUID id) {
        log.info("Deleting (deactivating) employee: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

        // Soft delete: set as inactive and set termination date
        employee.setActive(false);
        employee.setTerminationDate(LocalDateTime.now());

        employeeRepository.save(employee);
        log.info("Employee deactivated successfully: {}", id);
    }

    /**
     * Permanently delete an employee (hard delete)
     * @param id the employee ID
     */
    public void permanentlyDeleteEmployee(@NotNull UUID id) {
        log.warn("Permanently deleting employee: {}", id);

        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found: " + id);
        }

        employeeRepository.deleteById(id);
        log.info("Employee permanently deleted: {}", id);
    }

    // ========== ACTIVE EMPLOYEES OPERATIONS ==========

    /**
     * Find all active employees
     * @return list of active employees
     */
    @Transactional(readOnly = true)
    public List<Employee> findActiveEmployees() {
        return employeeRepository.findByActiveTrue();
    }

    /**
     * Find active employees ordered by name
     * @return list of active employees sorted by name
     */
    @Transactional(readOnly = true)
    public List<Employee> findActiveEmployeesOrderedByName() {
        return employeeRepository.findActiveEmployeesOrderedByName();
    }

    /**
     * Get all active employees as data maps (for planning algorithms)
     * @return list of employee data maps
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllActiveEmployeesAsData() {
        try {
            log.info("Fetching all active employees for planning");

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
            @SuppressWarnings("unchecked")
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

    /**
     * Count active employees
     * @return number of active employees
     */
    @Transactional(readOnly = true)
    public long countActiveEmployees() {
        return employeeRepository.countActiveEmployees();
    }

    // ========== EMPLOYEE SEARCH AND FILTERING ==========

    /**
     * Find employees by name pattern
     * @param namePattern pattern to search in names
     * @return list of matching employees
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesByName(@NotBlank String namePattern) {
        return employeeRepository.searchByNamePattern(namePattern.trim());
    }

    /**
     * Find employees by work capacity
     * @param capacity work capacity (HIGH, MEDIUM, LOW)
     * @return list of employees matching capacity
     */
    @Transactional(readOnly = true)
    public List<Employee> findEmployeesByWorkCapacity(@NotNull String capacity) {
        return employeeRepository.findByWorkCapacity(capacity.toUpperCase());
    }

    /**
     * Find available employees (active with positive work hours)
     * @return list of available employees
     */
    @Transactional(readOnly = true)
    public List<Employee> findAvailableEmployees() {
        return employeeRepository.findAvailableEmployees();
    }

    /**
     * Find employees by department
     * @param department the department name
     * @return list of employees in the department
     */
    @Transactional(readOnly = true)
    public List<Employee> findEmployeesByDepartment(@NotBlank String department) {
        return getAllEmployees(Pageable.unpaged()).getContent().stream()
                .filter(emp -> department.equalsIgnoreCase(emp.getDepartment()))
                .collect(Collectors.toList());
    }

    /**
     * Find employees by skill level
     * @param skillLevel the skill level
     * @return list of employees with the skill level
     */
    @Transactional(readOnly = true)
    public List<Employee> findEmployeesBySkillLevel(@NotNull Employee.SkillLevel skillLevel) {
        return getAllEmployees(Pageable.unpaged()).getContent().stream()
                .filter(emp -> skillLevel.equals(emp.getSkillLevel()))
                .collect(Collectors.toList());
    }

    // ========== WORKLOAD AND CAPACITY MANAGEMENT ==========

    /**
     * Get total work hours capacity per day for all active employees
     * @return total work hours capacity
     */
    @Transactional(readOnly = true)
    public double getTotalWorkHoursCapacity() {
        Double total = employeeRepository.getTotalWorkHoursCapacity();
        return total != null ? total : 0.0;
    }

    /**
     * Get average work hours per employee
     * @return average work hours
     */
    @Transactional(readOnly = true)
    public double getAverageWorkHours() {
        Double average = employeeRepository.getAverageWorkHours();
        return average != null ? average : DEFAULT_WORK_HOURS;
    }

    /**
     * Find top employees by work hours capacity
     * @param limit number of top employees to return
     * @return list of top employees
     */
    @Transactional(readOnly = true)
    public List<Employee> findTopEmployeesByWorkHours(int limit) {
        return employeeRepository.findTopEmployeesByWorkHours(Math.max(1, Math.min(limit, 100)));
    }

    /**
     * Get employee workload distribution
     * @return workload distribution map
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getWorkloadDistribution() {
        List<Employee> activeEmployees = findActiveEmployees();
        Map<String, Integer> distribution = new HashMap<>();

        distribution.put("HIGH", 0);      // >= 8 hours
        distribution.put("MEDIUM", 0);    // 6-8 hours
        distribution.put("LOW", 0);       // < 6 hours

        for (Employee employee : activeEmployees) {
            if (employee.getWorkHoursPerDay() != null) {
                String capacity = getWorkCapacityCategory(employee.getWorkHoursPerDay());
                distribution.put(capacity, distribution.get(capacity) + 1);
            }
        }

        return distribution;
    }

    // ========== EMPLOYEE STATISTICS ==========

    /**
     * Get employee statistics
     * @return statistics map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Basic counts
            long totalEmployees = employeeRepository.count();
            long activeEmployees = countActiveEmployees();
            long inactiveEmployees = totalEmployees - activeEmployees;

            // Work capacity stats
            double totalCapacity = getTotalWorkHoursCapacity();
            double averageWorkHours = getAverageWorkHours();

            // Distribution stats
            Map<String, Integer> workloadDistribution = getWorkloadDistribution();
            Map<String, Long> skillDistribution = getSkillLevelDistribution();

            stats.put("totalEmployees", totalEmployees);
            stats.put("activeEmployees", activeEmployees);
            stats.put("inactiveEmployees", inactiveEmployees);
            stats.put("totalWorkHoursCapacity", Math.round(totalCapacity * 100.0) / 100.0);
            stats.put("averageWorkHours", Math.round(averageWorkHours * 100.0) / 100.0);
            stats.put("workloadDistribution", workloadDistribution);
            stats.put("skillDistribution", skillDistribution);

            // Calculate utilization metrics
            if (activeEmployees > 0) {
                double avgUtilization = (averageWorkHours / DEFAULT_WORK_HOURS) * 100;
                stats.put("averageUtilization", Math.round(avgUtilization * 100.0) / 100.0);
            } else {
                stats.put("averageUtilization", 0.0);
            }

            stats.put("timestamp", LocalDateTime.now());
            stats.put("status", "success");

        } catch (Exception e) {
            log.error("Error calculating employee statistics: {}", e.getMessage());
            stats.put("status", "error");
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Get skill level distribution
     * @return skill level distribution map
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getSkillLevelDistribution() {
        Map<String, Long> distribution = new HashMap<>();

        for (Employee.SkillLevel skillLevel : Employee.SkillLevel.values()) {
            List<Employee> employees = findEmployeesBySkillLevel(skillLevel);
            distribution.put(skillLevel.name(), (long) employees.size());
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
        return employeeRepository.findInactiveEmployees();
    }

    // ========== UTILITY METHODS ==========

    /**
     * Check if email is available
     * @param email the email to check
     * @param excludeEmployeeId employee ID to exclude (for updates)
     * @return true if email is available
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(@NotNull @Email String email, UUID excludeEmployeeId) {
        return employeeRepository.isEmailAvailable(email.toLowerCase().trim(), excludeEmployeeId);
    }

    /**
     * Get work capacity category based on work hours
     * @param workHours work hours per day
     * @return capacity category (HIGH, MEDIUM, LOW)
     */
    private String getWorkCapacityCategory(double workHours) {
        if (workHours >= 8.0) return "HIGH";
        if (workHours >= 6.0) return "MEDIUM";
        return "LOW";
    }

    /**
     * Validate new employee before creation
     * @param employee the employee to validate
     */
    private void validateNewEmployee(Employee employee) {
        // Check email uniqueness
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + employee.getEmail());
        }

        // Validate work hours range
        if (employee.getWorkHoursPerDay() != null) {
            if (employee.getWorkHoursPerDay() < MIN_WORK_HOURS) {
                throw new IllegalArgumentException("Work hours must be at least " + MIN_WORK_HOURS);
            }
            if (employee.getWorkHoursPerDay() > MAX_WORK_HOURS) {
                throw new IllegalArgumentException("Work hours cannot exceed " + MAX_WORK_HOURS);
            }
        }

        // Validate names are not empty
        if (employee.getFirstName() == null || employee.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (employee.getLastName() == null || employee.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
    }

    // ========== BATCH OPERATIONS ==========

    /**
     * Batch update employee work hours
     * @param employeeIds list of employee IDs
     * @param newWorkHours new work hours to set
     * @return number of updated employees
     */
    public int batchUpdateWorkHours(List<UUID> employeeIds, double newWorkHours) {
        log.info("Batch updating work hours for {} employees to {}", employeeIds.size(), newWorkHours);

        if (newWorkHours < MIN_WORK_HOURS || newWorkHours > MAX_WORK_HOURS) {
            throw new IllegalArgumentException("Invalid work hours: " + newWorkHours);
        }

        int updatedCount = 0;
        for (UUID employeeId : employeeIds) {
            try {
                Optional<Employee> optEmployee = employeeRepository.findById(employeeId);
                if (optEmployee.isPresent()) {
                    Employee employee = optEmployee.get();
                    employee.setWorkHoursPerDay(newWorkHours);
                    employeeRepository.save(employee);
                    updatedCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to update work hours for employee {}: {}", employeeId, e.getMessage());
            }
        }

        log.info("Successfully updated work hours for {} out of {} employees", updatedCount, employeeIds.size());
        return updatedCount;
    }

    /**
     * Create employee with builder pattern
     * @param firstName first name
     * @param lastName last name
     * @param email email address
     * @return employee builder
     */
    public Employee.EmployeeBuilder createEmployeeBuilder(String firstName, String lastName, String email) {
        return Employee.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email);
    }
}