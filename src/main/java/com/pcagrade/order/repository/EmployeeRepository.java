package com.pcagrade.order.repository;

import com.pcagrade.order.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Employee entity management
 * Translated from EmployeRepository to EmployeeRepository with correct method names
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    /**
     * Find employees by active status
     * @param active true for active employees, false for inactive
     * @return list of employees with specified active status
     */
    List<Employee> findByActive(boolean active);

    /**
     * Find active employees
     * @return list of active employees
     */
    List<Employee> findByActiveTrue();

    /**
     * Find inactive employees
     * @return list of inactive employees
     */
    List<Employee> findByActiveFalse();

    /**
     * Find employee by email
     * @param email the email to search for
     * @return optional employee
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Find employees by first name containing (case insensitive)
     * @param firstName the first name to search for
     * @return list of employees with matching first name
     */
    List<Employee> findByFirstNameContainingIgnoreCase(String firstName);

    /**
     * Find employees by last name containing (case insensitive)
     * @param lastName the last name to search for
     * @return list of employees with matching last name
     */
    List<Employee> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Find employees by first name or last name containing (case insensitive)
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @return list of employees with matching first or last name
     */
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    /**
     * Find employees by skill level
     * @param skillLevel the skill level to filter by
     * @return list of employees with specified skill level
     */
    List<Employee> findBySkillLevel(Employee.SkillLevel skillLevel);

    /**
     * Count employees by active status
     * @param active true to count active employees, false for inactive
     * @return number of employees with specified active status
     */
    long countByActive(boolean active);

    /**
     * Count active employees
     * @return number of active employees
     */
    long countByActiveTrue();

    /**
     * Count inactive employees
     * @return number of inactive employees
     */
    long countByActiveFalse();

    /**
     * Find active employees (alternative method name for compatibility)
     * @return list of active employees
     */
    @Query("SELECT e FROM Employee e WHERE e.active = true ORDER BY e.lastName, e.firstName")
    List<Employee> findByIsActiveTrue();

    /**
     * Find active employees ordered by name
     * @return list of active employees sorted by name
     */
    @Query("SELECT e FROM Employee e WHERE e.active = true ORDER BY e.lastName, e.firstName")
    List<Employee> findActiveEmployeesOrderedByName();

    /**
     * Count active employees (alternative method)
     * @return number of active employees
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.active = true")
    long countActiveEmployees();

    /**
     * Check if email is available for a new employee or updating existing employee
     * @param email the email to check
     * @param excludeId employee ID to exclude from check (for updates)
     * @return true if email is available
     */
    @Query("SELECT COUNT(e) = 0 FROM Employee e WHERE e.email = :email AND (:excludeId IS NULL OR e.id != :excludeId)")
    boolean isEmailAvailable(@Param("email") String email, @Param("excludeId") UUID excludeId);

    /**
     * Find employees available for work (active and within work hours)
     * @return list of available employees
     */
    @Query("SELECT e FROM Employee e WHERE e.active = true AND e.workHoursPerDay > 0 ORDER BY e.lastName, e.firstName")
    List<Employee> findAvailableEmployees();

    /**
     * Find employees by work hours range
     * @param minHours minimum work hours per day
     * @param maxHours maximum work hours per day
     * @return list of employees within the work hours range
     */
    @Query("SELECT e FROM Employee e WHERE e.active = true AND e.workHoursPerDay BETWEEN :minHours AND :maxHours")
    List<Employee> findByWorkHoursRange(@Param("minHours") Double minHours, @Param("maxHours") Double maxHours);

    /**
     * Get employee statistics by skill level
     * @return list of objects containing skill level and count
     */
    @Query("SELECT e.skillLevel, COUNT(e) FROM Employee e WHERE e.active = true GROUP BY e.skillLevel")
    List<Object[]> getEmployeeStatisticsBySkillLevel();
}