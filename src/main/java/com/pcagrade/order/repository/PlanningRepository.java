package com.pcagrade.order.repository;

import com.pcagrade.order.entity.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Planning entity management
 * Translated from PlanificationRepository to PlanningRepository
 */
@Repository
public interface PlanningRepository extends JpaRepository<Planning, UUID> {

    /**
     * Find planning entries by employee and date range
     * @param employeeId the employee ID
     * @param startDate start date of the range
     * @param endDate end date of the range
     * @return list of planning entries sorted by date and start time
     */
    @Query("SELECT p FROM Planning p WHERE p.employeeId = :employeeId " +
            "AND p.planningDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.planningDate, p.startTime")
    List<Planning> findByEmployeeIdAndPlanningDateBetween(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find planning entries by date range
     * @param startDate start date of the range
     * @param endDate end date of the range
     * @return list of planning entries sorted by date and start time
     */
    @Query("SELECT p FROM Planning p WHERE p.planningDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.planningDate, p.startTime")
    List<Planning> findByPlanningDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find planning entries by order ID
     * @param orderId the order ID
     * @return list of planning entries for the specified order
     */
    @Query("SELECT p FROM Planning p WHERE p.orderId = :orderId ORDER BY p.planningDate, p.startTime")
    List<Planning> findByOrderId(@Param("orderId") UUID orderId);

    /**
     * Find incomplete planning entries
     * @return list of planning entries that are not completed
     */
    @Query("SELECT p FROM Planning p WHERE p.completed = false " +
            "ORDER BY p.planningDate, p.startTime")
    List<Planning> findIncompleteEntries();

    /**
     * Find planning entries by employee ID
     * @param employeeId the employee ID
     * @return list of planning entries for the specified employee
     */
    @Query("SELECT p FROM Planning p WHERE p.employeeId = :employeeId " +
            "ORDER BY p.planningDate DESC, p.startTime DESC")
    List<Planning> findByEmployeeId(@Param("employeeId") UUID employeeId);

    /**
     * Find planning entries for a specific date
     * @param planningDate the date to search for
     * @return list of planning entries for the specified date
     */
    @Query("SELECT p FROM Planning p WHERE p.planningDate = :planningDate " +
            "ORDER BY p.startTime")
    List<Planning> findByPlanningDate(@Param("planningDate") LocalDate planningDate);

    /**
     * Find planning entries by employee for a specific date
     * @param employeeId the employee ID
     * @param planningDate the planning date
     * @return list of planning entries for the employee on the specified date
     */
    @Query("SELECT p FROM Planning p WHERE p.employeeId = :employeeId AND p.planningDate = :planningDate " +
            "ORDER BY p.startTime")
    List<Planning> findByEmployeeIdAndPlanningDate(
            @Param("employeeId") UUID employeeId,
            @Param("planningDate") LocalDate planningDate);

    /**
     * Find overlapping planning entries for an employee
     * @param employeeId the employee ID
     * @param planningDate the date to check
     * @param startTime start time of the new entry
     * @param endTime end time of the new entry
     * @return list of overlapping planning entries
     */
    @Query("SELECT p FROM Planning p WHERE p.employeeId = :employeeId " +
            "AND p.planningDate = :planningDate " +
            "AND ((p.startTime <= :startTime AND p.endTime > :startTime) " +
            "OR (p.startTime < :endTime AND p.endTime >= :endTime) " +
            "OR (p.startTime >= :startTime AND p.endTime <= :endTime))")
    List<Planning> findOverlappingEntries(
            @Param("employeeId") UUID employeeId,
            @Param("planningDate") LocalDate planningDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * Get total planned minutes for an employee on a specific date
     * @param employeeId the employee ID
     * @param planningDate the date
     * @return total minutes planned
     */
    @Query("SELECT COALESCE(SUM(p.durationMinutes), 0) FROM Planning p " +
            "WHERE p.employeeId = :employeeId AND p.planningDate = :planningDate")
    Long getTotalPlannedMinutesForEmployeeAndDate(
            @Param("employeeId") UUID employeeId,
            @Param("planningDate") LocalDate planningDate);

    /**
     * Get workload statistics for all employees on a specific date
     * @param planningDate the date
     * @return list of workload data
     */
    @Query("SELECT p.employeeId, COUNT(p), SUM(p.durationMinutes) " +
            "FROM Planning p WHERE p.planningDate = :planningDate " +
            "GROUP BY p.employeeId")
    List<Object[]> getWorkloadStatsByDate(@Param("planningDate") LocalDate planningDate);

    /**
     * Find planning entries by status (completed/incomplete)
     * @param completed completion status
     * @return list of planning entries with specified completion status
     */
    List<Planning> findByCompletedOrderByPlanningDateDescStartTimeDesc(boolean completed);

    /**
     * Count planning entries by employee and date range
     * @param employeeId the employee ID
     * @param startDate start date
     * @param endDate end date
     * @return number of planning entries
     */
    @Query("SELECT COUNT(p) FROM Planning p WHERE p.employeeId = :employeeId " +
            "AND p.planningDate BETWEEN :startDate AND :endDate")
    Long countByEmployeeIdAndDateRange(
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find upcoming planning entries (from today onwards)
     * @return list of upcoming planning entries
     */
    @Query("SELECT p FROM Planning p WHERE p.planningDate >= CURRENT_DATE " +
            "ORDER BY p.planningDate, p.startTime")
    List<Planning> findUpcomingEntries();

    /**
     * Find overdue incomplete planning entries (past dates, not completed)
     * @return list of overdue planning entries
     */
    @Query("SELECT p FROM Planning p WHERE p.planningDate < CURRENT_DATE AND p.completed = false " +
            "ORDER BY p.planningDate DESC, p.startTime DESC")
    List<Planning> findOverdueIncompleteEntries();

    /**
     * Get planning efficiency statistics
     * @param startDate start date for analysis
     * @param endDate end date for analysis
     * @return efficiency statistics
     */
    @Query("SELECT " +
            "COUNT(CASE WHEN p.completed = true THEN 1 END) as completedCount, " +
            "COUNT(CASE WHEN p.completed = false THEN 1 END) as incompleteCount, " +
            "AVG(p.durationMinutes) as avgDuration " +
            "FROM Planning p WHERE p.planningDate BETWEEN :startDate AND :endDate")
    Object[] getPlanningEfficiencyStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Delete planning entries older than specified date
     * @param cutoffDate date before which to delete entries
     * @return number of deleted entries
     */
    @Query("DELETE FROM Planning p WHERE p.planningDate < :cutoffDate")
    int deleteOldEntries(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Find available time slots for an employee on a specific date
     * This is a helper method to find gaps in the schedule
     * @param employeeId the employee ID
     * @param planningDate the date
     * @return list of existing planning entries (to calculate gaps externally)
     */
    @Query("SELECT p FROM Planning p WHERE p.employeeId = :employeeId AND p.planningDate = :planningDate " +
            "ORDER BY p.startTime")
    List<Planning> findEmployeeScheduleForDate(
            @Param("employeeId") UUID employeeId,
            @Param("planningDate") LocalDate planningDate);
}