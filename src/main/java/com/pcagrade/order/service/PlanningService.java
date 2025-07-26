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
    public Planning updatePlanningEntry(@NotNull UUID planningId, @Valid @NotNull Planning updatedPlanning) {
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
    public Optional<Planning> getPlanningById(@NotNull UUID planningId) {
        return planningRepository.findById(planningId);
    }

    /**
     * Get planning entry by ID (string version for compatibility)
     * @param planningId the planning ID as string
     * @return planning entry if found
     */
    public Optional<Planning> getPlanningById(@NotNull String planningId) {
        try {
            UUID uuid = UUID.fromString(planningId);
            return planningRepository.findById(uuid);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for planning ID: {}", planningId);
            return Optional.empty();
        }
    }

    /**
     * Delete a planning entry
     * @param planningId the planning ID
     */
    public void deletePlanningEntry(@NotNull UUID planningId) {
        log.info("Deleting planning entry: {}", planningId);

        if (!planningRepository.existsById(planningId)) {
            throw new IllegalArgumentException("Planning entry not found with ID: " + planningId);
        }

        planningRepository.deleteById(planningId);
        log.info("Planning entry deleted successfully: {}", planningId);
    }

    /**
     * Delete a planning entry (string version for compatibility)
     * @param planningId the planning ID as string
     */
    public void deletePlanningEntry(@NotNull String planningId) {
        try {
            UUID uuid = UUID.fromString(planningId);
            deletePlanningEntry(uuid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for planning ID: " + planningId);
        }
    }

    // ========== QUERY METHODS ==========

    /**
     * Find planning entries by employee ID
     * @param employeeId the employee ID
     * @return list of planning entries for the employee
     */
    @Transactional(readOnly = true)
    public List<Planning> findPlanningsByEmployeeId(@NotNull UUID employeeId) {
        return planningRepository.findByEmployeeIdOrderByStartTimeAsc(employeeId);
    }

    /**
     * Find planning entries by employee ID (string version)
     * @param employeeId the employee ID as string
     * @return list of planning entries for the employee
     */
    @Transactional(readOnly = true)
    public List<Planning> findPlanningsByEmployeeId(@NotNull String employeeId) {
        return planningRepository.findByEmployeeIdOrderByStartTimeAsc(employeeId);
    }

    /**
     * Find planning entries by order ID
     * @param orderId the order ID
     * @return list of planning entries for the order
     */
    @Transactional(readOnly = true)
    public List<Planning> findPlanningsByOrderId(@NotNull UUID orderId) {
        return planningRepository.findByOrderIdOrderByStartTimeAsc(orderId);
    }

    /**
     * Find planning entries by order ID (string version)
     * @param orderId the order ID as string
     * @return list of planning entries for the order
     */
    @Transactional(readOnly = true)
    public List<Planning> findPlanningsByOrderId(@NotNull String orderId) {
        return planningRepository.findByOrderIdOrderByStartTimeAsc(orderId);
    }

    /**
     * Find planning entries between two dates
     * @param startDate start date
     * @param endDate end date
     * @return list of planning entries within the date range
     */
    @Transactional(readOnly = true)
    public List<Planning> findPlanningsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return planningRepository.findByStartTimeBetweenOrderByStartTimeAsc(startDate, endDate);
    }

    /**
     * Get all planning entries with pagination
     * @param pageable pagination information
     * @return page of planning entries
     */
    @Transactional(readOnly = true)
    public Page<Planning> getAllPlannings(Pageable pageable) {
        return planningRepository.findAll(pageable);
    }

    // ========== STATISTICS METHODS ==========

    /**
     * Get planning statistics
     * @return statistics map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPlanningStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Basic counts by status
            long scheduledCount = planningRepository.countByStatus(Planning.PlanningStatus.SCHEDULED);
            long inProgressCount = planningRepository.countByStatus(Planning.PlanningStatus.IN_PROGRESS);
            long completedCount = planningRepository.countByStatus(Planning.PlanningStatus.COMPLETED);
            long cancelledCount = planningRepository.countByStatus(Planning.PlanningStatus.CANCELLED);

            stats.put("scheduledCount", scheduledCount);
            stats.put("inProgressCount", inProgressCount);
            stats.put("completedCount", completedCount);
            stats.put("cancelledCount", cancelledCount);
            stats.put("totalCount", scheduledCount + inProgressCount + completedCount + cancelledCount);

            // Priority distribution
            long urgentCount = planningRepository.countByPriority(Planning.PlanningPriority.URGENT);
            long highCount = planningRepository.countByPriority(Planning.PlanningPriority.HIGH);
            long mediumCount = planningRepository.countByPriority(Planning.PlanningPriority.MEDIUM);
            long lowCount = planningRepository.countByPriority(Planning.PlanningPriority.LOW);

            stats.put("urgentCount", urgentCount);
            stats.put("highCount", highCount);
            stats.put("mediumCount", mediumCount);
            stats.put("lowCount", lowCount);

            // Recent planning entries (last 7 days)
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            long recentCount = planningRepository.countByCreatedAtAfter(sevenDaysAgo);
            stats.put("recentPlanningsCount", recentCount);

            // Employee workload distribution
            List<Object[]> workloadDistribution = planningRepository.findEmployeeWorkloadDistribution();
            stats.put("employeeWorkloadDistribution", workloadDistribution);

            stats.put("success", true);
            stats.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error calculating planning statistics", e);
            stats.put("success", false);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    // ========== AUTOMATIC PLANNING METHODS ==========

    /**
     * Execute automatic planning for orders since a specific date
     * @param day day of the month
     * @param month month (1-12)
     * @param year year
     * @return list of created planning entries
     */
    public List<Planning> executeAutomaticPlanning(int day, int month, int year) {
        log.info("Executing automatic planning from {}/{}/{}", day, month, year);

        try {
            LocalDate fromDate = LocalDate.of(year, month, day);

            // Get unassigned orders since the specified date
            List<Order> orders = getUnassignedOrdersSince(fromDate);
            log.info("Found {} unassigned orders since {}", orders.size(), fromDate);

            if (orders.isEmpty()) {
                log.info("No unassigned orders found for automatic planning");
                return new ArrayList<>();
            }

            // Get available employees
            List<Employee> availableEmployees = employeeRepository.findAllActive();
            log.info("Found {} available employees", availableEmployees.size());

            if (availableEmployees.isEmpty()) {
                log.warn("No available employees found for planning");
                return new ArrayList<>();
            }

            // Create planning entries
            List<Planning> plannings = createOptimalPlannings(orders, availableEmployees);

            // Save all planning entries
            List<Planning> savedPlannings = planningRepository.saveAll(plannings);
            log.info("Created {} planning entries successfully", savedPlannings.size());

            return savedPlannings;

        } catch (Exception e) {
            log.error("Error executing automatic planning", e);
            throw new RuntimeException("Failed to execute automatic planning: " + e.getMessage(), e);
        }
    }

    // ========== VALIDATION METHODS ==========

    /**
     * Validate new planning entry
     */
    private void validateNewPlanningEntry(Planning planning) {
        // Validate employee exists and is active
        Employee employee = employeeRepository.findById(planning.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + planning.getEmployeeId()));

        if (!employee.isActive()) {
            throw new IllegalArgumentException("Cannot assign planning to inactive employee: " + planning.getEmployeeId());
        }

        // Validate order exists
        if (!orderRepository.existsById(planning.getOrderId())) {
            throw new IllegalArgumentException("Order not found: " + planning.getOrderId());
        }

        // Validate time conflicts
        if (planning.getStartTime() != null && planning.getEstimatedEndTime() != null) {
            List<Planning> overlapping = planningRepository.findOverlappingEntries(
                    planning.getEmployeeId(),
                    planning.getStartTime(),
                    planning.getEstimatedEndTime()
            );

            if (!overlapping.isEmpty()) {
                throw new IllegalArgumentException("Planning conflicts with existing entries for employee: " + planning.getEmployeeId());
            }
        }

        // Validate working hours
        if (planning.getStartTime() != null) {
            int startHour = planning.getStartTime().getHour();
            if (startHour < DEFAULT_WORK_START_HOUR || startHour >= DEFAULT_WORK_END_HOUR) {
                throw new IllegalArgumentException("Planning start time must be within working hours (9 AM - 5 PM)");
            }
        }

        if (planning.getEstimatedEndTime() != null) {
            int endHour = planning.getEstimatedEndTime().getHour();
            if (endHour > DEFAULT_WORK_END_HOUR) {
                throw new IllegalArgumentException("Planning end time must be within working hours (9 AM - 5 PM)");
            }
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Get unassigned orders since a specific date
     */
    private List<Order> getUnassignedOrdersSince(LocalDate fromDate) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(fromDate.minusDays(1)))
                .filter(order -> order.getStatus() == Order.OrderStatus.PENDING)
                .collect(Collectors.toList());
    }

    /**
     * Create optimal planning assignments
     */
    private List<Planning> createOptimalPlannings(List<Order> orders, List<Employee> employees) {
        List<Planning> plannings = new ArrayList<>();

        // Initialize employee availability tracking
        Map<UUID, LocalDateTime> employeeAvailability = new HashMap<>();
        LocalDateTime workStartTime = LocalDate.now().atTime(DEFAULT_WORK_START_HOUR, 0);

        for (Employee employee : employees) {
            employeeAvailability.put(employee.getId(), workStartTime);
        }

        // Sort orders by priority (HIGH, MEDIUM, LOW)
        orders.sort((o1, o2) -> o2.getPriority().compareTo(o1.getPriority()));

        for (Order order : orders) {
            // Find best available employee
            UUID bestEmployeeId = null;
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
                planning.setPlanningDate(earliestTime.toLocalDate());
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
            case HIGH -> Planning.PlanningPriority.URGENT;
            case MEDIUM -> Planning.PlanningPriority.HIGH;
            case LOW -> Planning.PlanningPriority.MEDIUM;
        };
    }

    /**
     * Get available employees for planning
     * @return list of active employees
     */
    private List<Employee> getAvailableEmployees() {
        return employeeRepository.findAllActive();
    }

    /**
     * Calculate optimal start time for an employee
     */
    private LocalDateTime calculateOptimalStartTime(UUID employeeId, LocalDate date, int durationMinutes) {
        // Get existing plannings for the employee on the specified date
        List<Planning> existingPlannings = planningRepository.findByEmployeeIdAndPlanningDate(employeeId, date);

        LocalDateTime workStart = date.atTime(DEFAULT_WORK_START_HOUR, 0);
        LocalDateTime workEnd = date.atTime(DEFAULT_WORK_END_HOUR, 0);

        if (existingPlannings.isEmpty()) {
            return workStart;
        }

        // Sort by start time
        existingPlannings.sort(Comparator.comparing(Planning::getStartTime));

        // Find first available slot
        LocalDateTime currentTime = workStart;
        for (Planning planning : existingPlannings) {
            if (planning.getStartTime().isAfter(currentTime.plusMinutes(durationMinutes))) {
                return currentTime;
            }
            currentTime = planning.getEstimatedEndTime().plusMinutes(15); // 15 min buffer
        }

        // Check if there's time at the end of the day
        if (currentTime.plusMinutes(durationMinutes).isBefore(workEnd)) {
            return currentTime;
        }

        // No available slot today, try next day
        return calculateOptimalStartTime(employeeId, date.plusDays(1), durationMinutes);
    }

    // ========== COMPATIBILITY METHODS (for migration from PlanificationService) ==========

    /**
     * Legacy method for compatibility with existing code
     * @deprecated Use executeAutomaticPlanning() instead
     */
    @Deprecated
    public List<Planning> executerPlanificationAutomatique(int jour, int mois, int annee) {
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

    /**
     * Get planning entries for a specific date range as maps
     * @param startDate start date
     * @param endDate end date
     * @return list of planning maps
     */
    public List<Map<String, Object>> getPlanningsForDateRangeAsMap(LocalDate startDate, LocalDate endDate) {
        List<Planning> plannings = planningRepository.findByPlanningDateBetween(startDate, endDate);

        return plannings.stream().map(planning -> {
            Map<String, Object> planningMap = new HashMap<>();
            planningMap.put("id", planning.getId());
            planningMap.put("orderId", planning.getOrderId());
            planningMap.put("employeeId", planning.getEmployeeId());
            planningMap.put("planningDate", planning.getPlanningDate());
            planningMap.put("startTime", planning.getStartTime());
            planningMap.put("estimatedDurationMinutes", planning.getEstimatedDurationMinutes());
            planningMap.put("estimatedEndTime", planning.getEstimatedEndTime());
            planningMap.put("status", planning.getStatus().name());
            planningMap.put("priority", planning.getPriority().name());
            planningMap.put("progressPercentage", planning.getProgressPercentage());
            planningMap.put("notes", planning.getNotes());
            planningMap.put("createdAt", planning.getCreatedAt());
            planningMap.put("updatedAt", planning.getUpdatedAt());

            // Add employee and order details if needed
            try {
                Employee employee = employeeRepository.findById(planning.getEmployeeId()).orElse(null);
                if (employee != null) {
                    planningMap.put("employeeName", employee.getFullName());
                    planningMap.put("employeeEmail", employee.getEmail());
                }

                Order order = orderRepository.findById(planning.getOrderId()).orElse(null);
                if (order != null) {
                    planningMap.put("orderNumber", order.getOrderNumber());
                    planningMap.put("cardCount", order.getCardCount());
                }
            } catch (Exception e) {
                log.warn("Could not load related entities for planning {}", planning.getId());
            }

            return planningMap;
        }).collect(Collectors.toList());
    }

    /**
     * Get planning summary for an employee
     * @param employeeId the employee ID
     * @param startDate start date
     * @param endDate end date
     * @return planning summary map
     */
    public Map<String, Object> getEmployeePlanningSummary(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();

        try {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

            List<Planning> plannings = planningRepository.findByEmployeeIdAndPlanningDateBetween(
                    employeeId, startDate, endDate);

            summary.put("employeeId", employeeId);
            summary.put("employeeName", employee.getFullName());
            summary.put("totalPlannings", plannings.size());

            // Status breakdown
            Map<String, Long> statusCounts = plannings.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getStatus().name(),
                            Collectors.counting()
                    ));
            summary.put("statusBreakdown", statusCounts);

            // Total estimated time
            int totalMinutes = plannings.stream()
                    .mapToInt(Planning::getEstimatedDurationMinutes)
                    .sum();
            summary.put("totalEstimatedMinutes", totalMinutes);
            summary.put("totalEstimatedHours", totalMinutes / 60.0);

            // Completion rate
            long completedCount = plannings.stream()
                    .filter(p -> p.getStatus() == Planning.PlanningStatus.COMPLETED)
                    .count();
            double completionRate = plannings.isEmpty() ? 0.0 : (double) completedCount / plannings.size() * 100;
            summary.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

            summary.put("success", true);

        } catch (Exception e) {
            log.error("Error generating employee planning summary", e);
            summary.put("success", false);
            summary.put("error", e.getMessage());
        }

        return summary;
    }

    /**
     * Get workload analysis for all employees
     * @param startDate start date
     * @param endDate end date
     * @return workload analysis map
     */
    public Map<String, Object> getWorkloadAnalysis(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            List<Employee> activeEmployees = employeeRepository.findAllActive();
            List<Map<String, Object>> employeeWorkloads = new ArrayList<>();

            for (Employee employee : activeEmployees) {
                Map<String, Object> workload = getEmployeePlanningSummary(
                        employee.getId(), startDate, endDate);
                employeeWorkloads.add(workload);
            }

            analysis.put("employeeWorkloads", employeeWorkloads);
            analysis.put("totalEmployees", activeEmployees.size());
            analysis.put("periodStart", startDate);
            analysis.put("periodEnd", endDate);
            analysis.put("success", true);

        } catch (Exception e) {
            log.error("Error generating workload analysis", e);
            analysis.put("success", false);
            analysis.put("error", e.getMessage());
        }

        return analysis;
    }
}