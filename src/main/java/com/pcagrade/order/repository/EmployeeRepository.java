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
 * Translated from EmployeRepository to EmployeeRepository
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    /**
     * Find all active employees
     * @return list of active employees
     */
    List<Employee> findByActiveTrue();

    /**
     * Find employee by email address
     * @param email the email to search for
     * @return optional employee
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Find employees by last name
     * @param lastName the last name to search for
     * @return list of employees with matching last name
     */
    List<Employee> findByLastName(String lastName);

    /**
     * Find employees by first name
     * @param firstName the first name to search for
     * @return list of employees with matching first name
     */
    List<Employee> findByFirstName(String firstName);

    /**
     * Find employees by full name (first and last)
     * @param firstName the first name
     * @param lastName the last name
     * @return list of employees matching both names
     */
    List<Employee> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Find all active employees ordered by name
     * @return list of active employees sorted by last name, then first name
     */
    @Query("SELECT e FROM Employee e WHERE e.active = true ORDER BY e.lastName, e.firstName")
    List<Employee> findActiveEmployeesOrderedByName();

    /**
     * Count employees by active status
     * @param active whether to count active or inactive employees
     * @return number of employees with specified active status
     */
    long countByActive(boolean active);

    /**
     * Count all active employees
     * @return number of active employees
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.active = true")
    long countActiveEmployees();

    /**
     * Find employees with work hours in specified range
     * @param minHours minimum work hours per day
     * @param maxHours maximum work hours per day
     * @return list of employees within work hours range
     */
    @Query("SELECT e FROM Employee e WHERE e.workHoursPerDay BETWEEN :minHours AND :maxHours AND e.active = true")
    List<Employee> findByWorkHoursRange(@Param("minHours") double minHours, @Param("maxHours") double maxHours);

    /**
     * Find employees available for work (active with positive work hours)
     * @return list of available employees
     */
    @Query("SELECT e FROM Employee e WHERE e.active = true AND e.workHoursPerDay > 0 ORDER BY e.workHoursPerDay DESC")
    List<Employee> findAvailableEmployees();

    /**
     * Find employees by work capacity (high, medium, low)
     * High: >= 8 hours, Medium: 6-8 hours, Low: < 6 hours
     * @param capacity work capacity category
     * @return list of employees matching capacity
     */
    @Query("SELECT e FROM Employee e WHERE e.active = true AND " +
            "CASE " +
            "  WHEN :capacity = 'HIGH' THEN e.workHoursPerDay >= 8 " +
            "  WHEN :capacity = 'MEDIUM' THEN e.workHoursPerDay >= 6 AND e.workHoursPerDay < 8 " +
            "  WHEN :capacity = 'LOW' THEN e.workHoursPerDay < 6 " +
            "  ELSE false " +
            "END " +
            "ORDER BY e.workHoursPerDay DESC")
    List<Employee> findByWorkCapacity(@Param("capacity") String capacity);

    /**
     * Find employees created after specific date
     * @param creationDate the date after which to search
     * @return list of employees created after the specified date
     */
    @Query("SELECT e FROM Employee e WHERE e.creationDate >= :creationDate ORDER BY e.creationDate DESC")
    List<Employee> findEmployeesCreatedAfter(@Param("creationDate") java.time.LocalDateTime creationDate);

    /**
     * Search employees by name pattern (case insensitive)
     * @param namePattern pattern to search in first or last name
     * @return list of employees matching the name pattern
     */
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
            "AND e.active = true " +
            "ORDER BY e.lastName, e.firstName")
    List<Employee> searchByNamePattern(@Param("namePattern") String namePattern);

    /**
     * Get total work hours capacity per day for all active employees
     * @return sum of work hours for all active employees
     */
    @Query("SELECT COALESCE(SUM(e.workHoursPerDay), 0) FROM Employee e WHERE e.active = true")
    Double getTotalWorkHoursCapacity();

    /**
     * Get average work hours per employee
     * @return average work hours per day
     */
    @Query("SELECT AVG(e.workHoursPerDay) FROM Employee e WHERE e.active = true AND e.workHoursPerDay > 0")
    Double getAverageWorkHours();

    /**
     * Find top N employees by work hours capacity
     * @param limit number of top employees to return
     * @return list of top employees by work hours
     */
    @Query(value = "SELECT * FROM j_employee e WHERE e.active = true ORDER BY e.work_hours_per_day DESC LIMIT :limit",
            nativeQuery = true)
    List<Employee> findTopEmployeesByWorkHours(@Param("limit") int limit);

    /**
     * Check if email is already taken by another employee
     * @param email email to check
     * @param excludeId employee ID to exclude from check (for updates)
     * @return true if email is available
     */
    @Query("SELECT COUNT(e) = 0 FROM Employee e WHERE e.email = :email AND (:excludeId IS NULL OR e.id != :excludeId)")
    boolean isEmailAvailable(@Param("email") String email, @Param("excludeId") UUID excludeId);

    /**
     * Find inactive employees that could be reactivated
     * @return list of inactive employees
     */
    @Query("SELECT e FROM Employee e WHERE e.active = false ORDER BY e.lastName, e.firstName")
    List<Employee> findInactiveEmployees();
}