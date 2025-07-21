package com.pcagrade.order.service;

import com.pcagrade.order.entity.Planning;
import com.pcagrade.order.entity.Order;
import com.pcagrade.order.entity.Employee;
import com.pcagrade.order.repository.PlanningRepository;
import com.pcagrade.order.repository.OrderRepository;
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
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing planning and scheduling operations
 * Translated from PlanificationService to PlanningService with enhanced functionality
 * Handles Pokemon card order scheduling across multiple employees
 */
@Service
@Transactional
@Validated
@Slf4j
public class PlanningService {

    private static final int DEFAULT_WORK_START_HOUR = 9;  // 9 AM
    private static final int DEFAULT_WORK_END_HOUR = 17;   // 5 PM
    private static final int DEFAULT_BREAK_MINUTES = 60;   // 1 hour lunch break
    private static final int MAX_PLANNING_DAYS_AHEAD = 365; // 1 year
    private static final int CARD_PROCESSING_TIME_MINUTES = 3; // 3 minutes per card
    private static final int DEFAULT_HISTORICAL_DAYS = 30; // 30 days for historical planning

    @Autowired
    private PlanningRepository planningRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    // ========== CRUD OPERATIONS ==========

    /**
     * Create a new planning entry
     * @param planning the planning entry to create
     * @return created planning entry
     */
    public Planning createPlanningEntry(@Valid @NotNull Planning planning) {
        log.info("Creating new planning entry for order {} and employee {}",
                planning.getOrderId(), planning.getEmployeeId());

        // Validate business rules
        validateNewPlanningEntry(planning);

        // Set default values
        if (planning.getStatus() == null) {
            planning.setStatus(Planning.PlanningStatus.SCHEDULED);
        }
        if (planning.getPriority() == null) {
            planning.setPriority(Planning.PlanningPriority.MEDIUM);
        }
        if (planning.getProgressPercentage() == null) {
            planning.setProgressPercentage(0);
        }

        // Calculate estimated end time if not provided
        if (planning.getEstimatedEndTime() == null && planning.getEstimatedDurationMinutes() != null) {
            planning.setEstimatedEndTime(
                    planning.getStartTime().plusMinutes(planning.getEstimatedDurationMinutes())
            );
        }

        // Set audit fields
        planning.setCreatedAt(LocalDateTime.now());
        planning.setUpdatedAt(LocalDateTime.now());

        Planning savedPlanning = planningRepository.save(planning);
        log.info("Planning entry created with ID: {}", savedPlanning.getId());

        return savedPlanning;
    }

    /**
     * Update an existing planning entry
     * @param planningId the ID of the planning to update
     * @param updatedPlanning the updated planning data
     * @return updated planning entry
     */
    public Planning updatePlanningEntry(@NotNull String planningId, @Valid @NotNull Planning updatedPlanning) {
        log.info("Updating planning entry with ID: {}", planningId);

        Planning existingPlanning = planningRepository.findById(planningId)
                .orElseThrow(() -> new RuntimeException("Planning entry not found with ID: " + planningId));

        // Update allowed fields
        existingPlanning.setStartTime(updatedPlanning.getStartTime());
        existingPlanning.setEstimatedDurationMinutes(updatedPlanning.getEstimatedDurationMinutes());
        existingPlanning.setEstimatedEndTime(updatedPlanning.getEstimatedEndTime());
        existingPlanning.setStatus(updatedPlanning.getStatus());
        existingPlanning.setPriority(updatedPlanning.getPriority());
        existingPlanning.setProgressPercentage(updatedPlanning.getProgressPercentage());
        existingPlanning.setNotes(updatedPlanning.getNotes());
        existingPlanning.setUpdatedAt(LocalDateTime.now());

        // If marked as completed, set actual end time
        if (updatedPlanning.getStatus() == Planning.PlanningStatus.COMPLETED
                && existingPlanning.getActualEndTime() == null) {
            existingPlanning.setActualEndTime(LocalDateTime.now());
            existingPlanning.setProgressPercentage(100);
        }

        Planning savedPlanning = planningRepository.save(existingPlanning);
        log.info("Planning entry updated successfully");

        return savedPlanning;
    }

    /**
     * Get planning entry by ID
     * @param planningId the planning ID
     * @return planning entry if found
     */
    public Optional<Planning> getPlanningById(@NotNull String planningId) {
        return planningRepository.findById(planningId);
    }

    /**
     * Get all planning entries with pagination
     * @param pageable pagination information
     * @return page of planning entries
     */
    public Page<Planning> getAllPlannings(Pageable pageable) {
        return planningRepository.findAll(pageable);
    }

    /**
     * Delete a planning entry
     * @param planningId the ID of the planning to delete
     */
    public void deletePlanning(@NotNull String planningId) {
        log.info("Deleting planning entry with ID: {}", planningId);

        if (!planningRepository.existsById(planningId)) {
            throw new RuntimeException("Planning entry not found with ID: " + planningId);
        }

        planningRepository.deleteById(planningId);
        log.info("Planning entry deleted successfully");
    }

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Execute automatic planning for orders from a specific date
     * @param startDate the date from which to start planning
     * @return planning results summary
     */
    @Transactional
    public Map<String, Object> executeAutomaticPlanning(@NotNull LocalDate startDate) {
        long executionStart = System.currentTimeMillis();
        log.info("Starting automatic planning from date: {}", startDate);

        Map<String, Object> result = new HashMap<>();

        try {
            // Get orders to plan
            List<Order> ordersToSchedule = getOrdersToSchedule(startDate);
            if (ordersToSchedule.isEmpty()) {
                return createErrorResult("No orders found to schedule from " + startDate);
            }

            // Get available employees
            List<Employee> availableEmployees = getAvailableEmployees();
            if (availableEmployees.isEmpty()) {
                return createErrorResult("No employees available for scheduling");
            }

            log.info("Found {} orders to schedule with {} available employees",
                    ordersToSchedule.size(), availableEmployees.size());

            // Sort orders by priority (high priority first)
            ordersToSchedule.sort((o1, o2) ->
                    Integer.compare(o2.getPriority().ordinal(), o1.getPriority().ordinal()));

            // Execute planning algorithm
            List<Planning> createdPlannings = executeGreedyPlanningAlgorithm(ordersToSchedule, availableEmployees);

            // Save all planning entries
            planningRepository.saveAll(createdPlannings);

            // Prepare result
            long executionTime = System.currentTimeMillis() - executionStart;
            result.put("success", true);
            result.put("message", "Automatic planning completed successfully");
            result.put("ordersScheduled", createdPlannings.size());
            result.put("totalOrders", ordersToSchedule.size());
            result.put("executionTimeMs", executionTime);
            result.put("algorithm", "GREEDY");
            result.put("timestamp", LocalDateTime.now());

            log.info("Automatic planning completed: {} out of {} orders scheduled in {}ms",
                    createdPlannings.size(), ordersToSchedule.size(), executionTime);

            return result;

        } catch (Exception e) {
            log.error("Error during automatic planning", e);
            result.put("success", false);
            result.put("message", "Error during automatic planning: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
            return result;
        }
    }

    /**
     * Execute automatic planning with default parameters (last 30 days)
     * @return planning results summary
     */
    public Map<String, Object> executeAutomaticPlanning() {
        LocalDate startDate = LocalDate.now().minusDays(DEFAULT_HISTORICAL_DAYS);
        return executeAutomaticPlanning(startDate);
    }

    /**
     * Execute automatic planning for a specific date range
     * @param day day of the start date
     * @param month month of the start date
     * @param year year of the start date
     * @return planning results summary
     */
    public Map<String, Object> executeAutomaticPlanning(int day, int month, int year) {
        try {
            LocalDate startDate = LocalDate.of(year, month, day);
            Map<String, Object> result = executeAutomaticPlanning(startDate);

            // Add input parameters to result
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("day", day);
            parameters.put("month", month);
            parameters.put("year", year);
            parameters.put("startDate", startDate.toString());

            result.put("parameters", parameters);
            return result;

        } catch (Exception e) {
            log.error("Error in automatic planning with parameters: {}/{}/{}", day, month, year, e);
            return createErrorResult("Invalid date parameters: " + day + "/" + month + "/" + year);
        }
    }

    /**
     * Get planning entries for a specific employee
     * @param employeeId the employee ID
     * @return list of planning entries for the employee
     */
    public List<Planning> getPlanningsByEmployee(@NotNull String employeeId) {
        return planningRepository.findByEmployeeIdOrderByStartTimeAsc(employeeId);
    }

    /**
     * Get planning entries for a specific order
     * @param orderId the order ID
     * @return list of planning entries for the order
     */
    public List<Planning> getPlanningsByOrder(@NotNull String orderId) {
        return planningRepository.findByOrderIdOrderByStartTimeAsc(orderId);
    }

    /**
     * Get planning entries for a specific date range
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of planning entries in the date range
     */
    public List<Planning> getPlanningsByDateRange(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return planningRepository.findByStartTimeBetweenOrderByStartTimeAsc(startDateTime, endDateTime);
    }

    /**
     * Get planning statistics for reporting
     * @return map containing various planning statistics
     */
    public Map<String, Object> getPlanningStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Total planning entries
            long totalPlannings = planningRepository.count();
            stats.put("totalPlannings", totalPlannings);

            // Planning by status
            Map<String, Long> statusCounts = new HashMap<>();
            for (Planning.PlanningStatus status : Planning.PlanningStatus.values()) {
                long count = planningRepository.countByStatus(status);
                statusCounts.put(status.name(), count);
            }
            stats.put("planningsByStatus", statusCounts);

            // Planning by priority
            Map<String, Long> priorityCounts = new HashMap<>();
            for (Planning.PlanningPriority priority : Planning.PlanningPriority.values()) {
                long count = planningRepository.countByPriority(priority);
                priorityCounts.put(priority.name(), count);
            }
            stats.put("planningsByPriority", priorityCounts);

            // Recent planning activity (last 7 days)
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            long recentPlannings = planningRepository.countByCreatedAtAfter(sevenDaysAgo);
            stats.put("recentPlannings", recentPlannings);

            // Employee workload distribution
            List<Object[]> employeeWorkload = planningRepository.findEmployeeWorkloadDistribution();
            Map<String, Long> workloadMap = employeeWorkload.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0], // employeeId
                            row -> (Long) row[1]    // planning count
                    ));
            stats.put("employeeWorkload", workloadMap);

            stats.put("success", true);
            stats.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error getting planning statistics", e);
            stats.put("success", false);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Validate a new planning entry according to business rules
     */
    private void validateNewPlanningEntry(Planning planning) {
        // Check if employee exists and is active
        Employee employee = employeeRepository.findById(planning.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + planning.getEmployeeId()));

        if (!employee.getIsActive()) {
            throw new RuntimeException("Cannot assign planning to inactive employee: " + planning.getEmployeeId());
        }

        // Check if order exists
        Order order = orderRepository.findById(planning.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + planning.getOrderId()));

        // Check for scheduling conflicts
        if (hasSchedulingConflict(planning)) {
            throw new RuntimeException("Scheduling conflict detected for employee " +
                    planning.getEmployeeId() + " at " + planning.getStartTime());
        }

        // Validate start time is not in the past (allow some tolerance)
        if (planning.getStartTime().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new RuntimeException("Cannot schedule planning in the past");
        }

        // Validate planning is not too far in the future
        if (planning.getStartTime().isAfter(LocalDateTime.now().plusDays(MAX_PLANNING_DAYS_AHEAD))) {
            throw new RuntimeException("Cannot schedule planning more than " + MAX_PLANNING_DAYS_AHEAD + " days ahead");
        }
    }

    /**
     * Check if there's a scheduling conflict for the given planning
     */
    private boolean hasSchedulingConflict(Planning planning) {
        if (planning.getEstimatedEndTime() == null) {
            return false; // Can't check conflict without end time
        }

        List<Planning> existingPlannings = planningRepository
                .findByEmployeeIdAndStartTimeBetween(
                        planning.getEmployeeId(),
                        planning.getStartTime().minusMinutes(1),
                        planning.getEstimatedEndTime().plusMinutes(1)
                );

        return !existingPlannings.isEmpty();
    }

    /**
     * Get orders that need to be scheduled from a given date
     */
    private List<Order> getOrdersToSchedule(LocalDate fromDate) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();

        // Get orders that:
        // 1. Were created from the specified date
        // 2. Don't have completed planning
        // 3. Are not cancelled

        String jpql = """
            SELECT DISTINCT o FROM Order o 
            LEFT JOIN Planning p ON o.id = p.orderId 
            WHERE o.orderDate >= :fromDate 
            AND o.status != 'CANCELLED'
            AND (p.id IS NULL OR p.status != 'COMPLETED')
            ORDER BY o.priority DESC, o.orderDate ASC
            """;

        return entityManager.createQuery(jpql, Order.class)
                .setParameter("fromDate", fromDateTime)
                .getResultList();
    }

    /**
     * Get list of available employees for planning
     */
    private List<Employee> getAvailableEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }

    /**
     * Execute greedy planning algorithm
     * Assigns orders to employees based on availability and priority
     */
    private List<Planning> executeGreedyPlanningAlgorithm(List<Order> orders, List<Employee> employees) {
        List<Planning> plannings = new ArrayList<>();

        // Track employee availability (next available time)
        Map<String, LocalDateTime> employeeAvailability = new HashMap<>();
        for (Employee employee : employees) {
            employeeAvailability.put(employee.getId(), LocalDateTime.now());
        }

        for (Order order : orders) {
            // Find employee with earliest availability
            String bestEmployeeId = null;
            LocalDateTime earliestTime = null;

            for (Employee employee : employees) {
                LocalDateTime availableTime = employeeAvailability.get(employee.getId());
                if (earliestTime == null || availableTime.isBefore(earliestTime)) {
                    earliestTime = availableTime;
                    bestEmployeeId = employee.getId();
                }
            }

            if (bestEmployeeId != null && earliestTime != null) {
                // Calculate duration based on card count
                int durationMinutes = order.getCardCount() * CARD_PROCESSING_TIME_MINUTES;

                // Create planning entry
                Planning planning = new Planning();
                planning.setOrderId(order.getId());
                planning.setEmployeeId(bestEmployeeId);
                planning.setStartTime(earliestTime);
                planning.setEstimatedDurationMinutes(durationMinutes);
                planning.setEstimatedEndTime(earliestTime.plusMinutes(durationMinutes));
                planning.setStatus(Planning.PlanningStatus.SCHEDULED);
                planning.setPriority(convertOrderPriorityToPlanningPriority(order.getPriority()));
                planning.setProgressPercentage(0);
                planning.setCreatedAt(LocalDateTime.now());
                planning.setUpdatedAt(LocalDateTime.now());

                plannings.add(planning);

                // Update employee availability
                employeeAvailability.put(bestEmployeeId,
                        earliestTime.plusMinutes(durationMinutes + 15)); // 15 min buffer between tasks
            }
        }

        return plannings;
    }

    /**
     * Convert Order priority to Planning priority
     */
    private Planning.PlanningPriority convertOrderPriorityToPlanningPriority(Order.OrderPriority orderPriority) {
        return switch (orderPriority) {
            case HIGH -> Planning.PlanningPriority.HIGH;
            case MEDIUM -> Planning.PlanningPriority.MEDIUM;
            case LOW -> Planning.PlanningPriority.LOW;
        };
    }

    /**
     * Create error result map
     */
    private Map<String, Object> createErrorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("timestamp", LocalDateTime.now());
        return result;
    }

    // ========== COMPATIBILITY METHODS (for migration from PlanificationService) ==========

    /**
     * Legacy method for compatibility with existing code
     * @deprecated Use executeAutomaticPlanning() instead
     */
    @Deprecated
    public Map<String, Object> executerPlanificationAutomatique() {
        log.warn("Using deprecated method executerPlanificationAutomatique(), please use executeAutomaticPlanning()");
        return executeAutomaticPlanning();
    }

    /**
     * Legacy method for compatibility with existing code
     * @deprecated Use executeAutomaticPlanning(int, int, int) instead
     */
    @Deprecated
    public Map<String, Object> executerPlanificationAutomatique(int jour, int mois, int annee) {
        log.warn("Using deprecated method executerPlanificationAutomatique(), please use executeAutomaticPlanning()");
        return executeAutomaticPlanning(jour, mois, annee);
    }

    /**
     * Get all plannings as Map objects (for compatibility with legacy frontend)
     * @return list of planning maps
     */
    public List<Map<String, Object>> getAllPlanningsAsMap() {
        List<Planning> plannings = planningRepository.findAll();

        return plannings.stream().map(planning -> {
            Map<String, Object> planningMap = new HashMap<>();
            planningMap.put("id", planning.getId());
            planningMap.put("orderId", planning.getOrderId());
            planningMap.put("employeeId", planning.getEmployeeId());
            planningMap.put("startTime", planning.getStartTime());
            planningMap.put("estimatedDurationMinutes", planning.getEstimatedDurationMinutes());
            planningMap.put("estimatedEndTime", planning.getEstimatedEndTime());
            planningMap.put("actualEndTime", planning.getActualEndTime());
            planningMap.put("status", planning.getStatus().name());
            planningMap.put("priority", planning.getPriority().name());
            planningMap.put("progressPercentage", planning.getProgressPercentage());
            planningMap.put("notes", planning.getNotes());
            planningMap.put("createdAt", planning.getCreatedAt());
            planningMap.put("updatedAt", planning.getUpdatedAt());
            return planningMap;
        }).collect(Collectors.toList());
    }
}