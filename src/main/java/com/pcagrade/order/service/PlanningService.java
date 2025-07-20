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

        // Calculate estimated end time
        planning.setEstimatedEndTime(planning.calculateEndTime());

        Planning savedPlanning = planningRepository.save(planning);
        log.info("Planning entry created successfully with ID: {}", savedPlanning.getId());
        return savedPlanning;
    }

    /**
     * Update an existing planning entry
     * @param planning the planning entry to update
     * @return updated planning entry
     */
    public Planning updatePlanningEntry(@Valid @NotNull Planning planning) {
        log.info("Updating planning entry: {}", planning.getId());

        if (!planningRepository.existsById(planning.getId())) {
            throw new IllegalArgumentException("Planning entry not found with ID: " + planning.getId());
        }

        // Validate time conflicts (excluding current entry)
        validateTimeConflicts(planning, planning.getId());

        Planning updatedPlanning = planningRepository.save(planning);
        log.info("Planning entry updated successfully: {}", updatedPlanning.getId());
        return updatedPlanning;
    }

    /**
     * Find planning entry by ID
     * @param id the planning ID
     * @return optional planning entry
     */
    @Transactional(readOnly = true)
    public Optional<Planning> findById(@NotNull UUID id) {
        return planningRepository.findById(id);
    }

    /**
     * Get all planning entries with pagination
     * @param pageable pagination information
     * @return page of planning entries
     */
    @Transactional(readOnly = true)
    public Page<Planning> getAllPlanningEntries(Pageable pageable) {
        return planningRepository.findAll(pageable);
    }

    /**
     * Delete a planning entry
     * @param id the planning ID
     */
    public void deletePlanningEntry(@NotNull UUID id) {
        log.info("Deleting planning entry: {}", id);

        if (!planningRepository.existsById(id)) {
            throw new IllegalArgumentException("Planning entry not found with ID: " + id);
        }

        planningRepository.deleteById(id);
        log.info("Planning entry deleted successfully: {}", id);
    }

    // ========== PLANNING QUERIES ==========

    /**
     * Find planning entries by employee and date range
     * @param employeeId the employee ID
     * @param startDate start date
     * @param endDate end date
     * @return list of planning entries
     */
    @Transactional(readOnly = true)
    public List<Planning> findPlanningByEmployeeAndDateRange(@NotNull UUID employeeId,
                                                             @NotNull LocalDate startDate,
                                                             @NotNull LocalDate endDate) {
        return planningRepository.findByEmployeeIdAndPlanningDateBetween(employeeId, startDate, endDate);
    }

    /**
     * Find planning entries by date range
     * @param startDate start date
     * @param endDate end date
     * @return list of planning entries
     */
    @Transactional(readOnly = true)
    public List<Planning> findPlanningByDateRange(@NotNull LocalDate startDate, @NotNull LocalDate endDate) {
        return planningRepository.findByPlanningDateBetween(startDate, endDate);
    }

    /**
     * Find planning entries for a specific date
     * @param planningDate the date
     * @return list of planning entries
     */
    @Transactional(readOnly = true)
    public List<Planning> findPlanningByDate(@NotNull LocalDate planningDate) {
        return planningRepository.findByPlanningDate(planningDate);
    }

    /**
     * Find planning entries by order
     * @param orderId the order ID
     * @return list of planning entries for the order
     */
    @Transactional(readOnly = true)
    public List<Planning> findPlanningByOrder(@NotNull UUID orderId) {
        return planningRepository.findByOrderId(orderId);
    }

    /**
     * Find incomplete planning entries
     * @return list of incomplete planning entries
     */
    @Transactional(readOnly = true)
    public List<Planning> findIncompletePlanningEntries() {
        return planningRepository.findIncompleteEntries();
    }

    /**
     * Find upcoming planning entries (from today onwards)
     * @return list of upcoming planning entries
     */
    @Transactional(readOnly = true)
    public List<Planning> findUpcomingPlanningEntries() {
        return planningRepository.findUpcomingEntries();
    }

    /**
     * Find overdue incomplete planning entries
     * @return list of overdue planning entries
     */
    @Transactional(readOnly = true)
    public List<Planning> findOverduePlanningEntries() {
        return planningRepository.findOverdueIncompleteEntries();
    }

    // ========== WORKLOAD AND CAPACITY ==========

    /**
     * Get total planned minutes for an employee on a specific date
     * @param employeeId the employee ID
     * @param planningDate the date
     * @return total planned minutes
     */
    @Transactional(readOnly = true)
    public long getTotalPlannedMinutes(@NotNull UUID employeeId, @NotNull LocalDate planningDate) {
        Long total = planningRepository.getTotalPlannedMinutesForEmployeeAndDate(employeeId, planningDate);
        return total != null ? total : 0L;
    }

    /**
     * Get workload statistics for all employees on a specific date
     * @param planningDate the date
     * @return workload statistics map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getWorkloadStatistics(@NotNull LocalDate planningDate) {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Object[]> workloadData = planningRepository.getWorkloadStatsByDate(planningDate);

            List<Map<String, Object>> employeeWorkloads = new ArrayList<>();
            long totalPlannedMinutes = 0;
            int employeeCount = 0;

            for (Object[] row : workloadData) {
                UUID employeeId = (UUID) row[0];
                Long taskCount = (Long) row[1];
                Long plannedMinutes = (Long) row[2];

                // Get employee details
                Optional<Employee> optEmployee = employeeRepository.findById(employeeId);
                if (optEmployee.isPresent()) {
                    Employee employee = optEmployee.get();

                    Map<String, Object> employeeWorkload = new HashMap<>();
                    employeeWorkload.put("employeeId", employeeId.toString());
                    employeeWorkload.put("employeeName", employee.getFullName());
                    employeeWorkload.put("taskCount", taskCount);
                    employeeWorkload.put("plannedMinutes", plannedMinutes);
                    employeeWorkload.put("plannedHours", Math.round((plannedMinutes / 60.0) * 100.0) / 100.0);

                    // Calculate utilization
                    double maxMinutes = employee.getWorkMinutesPerDay();
                    double utilization = (plannedMinutes / maxMinutes) * 100;
                    employeeWorkload.put("utilization", Math.round(utilization * 100.0) / 100.0);

                    // Determine workload status
                    String status = "NORMAL";
                    if (utilization > 100) status = "OVERLOADED";
                    else if (utilization > 80) status = "BUSY";
                    else if (utilization < 50) status = "AVAILABLE";
                    employeeWorkload.put("status", status);

                    employeeWorkloads.add(employeeWorkload);
                    totalPlannedMinutes += plannedMinutes;
                    employeeCount++;
                }
            }

            stats.put("date", planningDate.toString());
            stats.put("employeeWorkloads", employeeWorkloads);
            stats.put("totalEmployees", employeeCount);
            stats.put("totalPlannedMinutes", totalPlannedMinutes);
            stats.put("totalPlannedHours", Math.round((totalPlannedMinutes / 60.0) * 100.0) / 100.0);

            if (employeeCount > 0) {
                double avgPlannedMinutes = (double) totalPlannedMinutes / employeeCount;
                stats.put("averagePlannedMinutes", Math.round(avgPlannedMinutes * 100.0) / 100.0);
                stats.put("averagePlannedHours", Math.round((avgPlannedMinutes / 60.0) * 100.0) / 100.0);
            }

            stats.put("status", "success");

        } catch (Exception e) {
            log.error("Error calculating workload statistics for {}: {}", planningDate, e.getMessage());
            stats.put("status", "error");
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Check if employee is available at specific time
     * @param employeeId the employee ID
     * @param planningDate the date
     * @param startTime start time
     * @param durationMinutes duration in minutes
     * @return true if available
     */
    @Transactional(readOnly = true)
    public boolean isEmployeeAvailable(@NotNull UUID employeeId,
                                       @NotNull LocalDate planningDate,
                                       @NotNull LocalTime startTime,
                                       @Positive int durationMinutes) {
        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        List<Planning> conflicts = planningRepository.findOverlappingEntries(
                employeeId, planningDate, startTime, endTime);

        return conflicts.isEmpty();
    }

    /**
     * Find available time slots for an employee on a specific date
     * @param employeeId the employee ID
     * @param planningDate the date
     * @param minDurationMinutes minimum duration needed
     * @return list of available time slots
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findAvailableTimeSlots(@NotNull UUID employeeId,
                                                            @NotNull LocalDate planningDate,
                                                            @Positive int minDurationMinutes) {
        List<Map<String, Object>> availableSlots = new ArrayList<>();

        // Get employee's existing schedule for the date
        List<Planning> existingPlanning = planningRepository.findEmployeeScheduleForDate(employeeId, planningDate);

        // Get employee work hours
        Optional<Employee> optEmployee = employeeRepository.findById(employeeId);
        if (optEmployee.isEmpty()) {
            return availableSlots;
        }

        Employee employee = optEmployee.get();
        int workMinutesPerDay = employee.getWorkMinutesPerDay();

        // Define work day boundaries
        LocalTime workStart = LocalTime.of(DEFAULT_WORK_START_HOUR, 0);
        LocalTime workEnd = LocalTime.of(DEFAULT_WORK_END_HOUR, 0);

        // Sort existing planning by start time
        existingPlanning.sort((p1, p2) -> p1.getStartTime().compareTo(p2.getStartTime()));

        // Find gaps in the schedule
        LocalTime currentTime = workStart;

        for (Planning planning : existingPlanning) {
            LocalTime planningStart = planning.getStartTime();

            // Check if there's a gap before this planning
            if (currentTime.isBefore(planningStart)) {
                long gapMinutes = java.time.Duration.between(currentTime, planningStart).toMinutes();
                if (gapMinutes >= minDurationMinutes) {
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("startTime", currentTime.toString());
                    slot.put("endTime", planningStart.toString());
                    slot.put("durationMinutes", gapMinutes);
                    availableSlots.add(slot);
                }
            }

            // Move current time to after this planning
            LocalTime planningEnd = planning.calculateEndTime();
            if (planningEnd != null && planningEnd.isAfter(currentTime)) {
                currentTime = planningEnd;
            }
        }

        // Check if there's time left at the end of the work day
        if (currentTime.isBefore(workEnd)) {
            long remainingMinutes = java.time.Duration.between(currentTime, workEnd).toMinutes();
            if (remainingMinutes >= minDurationMinutes) {
                Map<String, Object> slot = new HashMap<>();
                slot.put("startTime", currentTime.toString());
                slot.put("endTime", workEnd.toString());
                slot.put("durationMinutes", remainingMinutes);
                availableSlots.add(slot);
            }
        }

        return availableSlots;
    }

    // ========== PLANNING STATUS OPERATIONS ==========

    /**
     * Start a planning task
     * @param planningId the planning ID
     * @return updated planning entry
     */
    public Planning startTask(@NotNull UUID planningId) {
        log.info("Starting task: {}", planningId);

        Planning planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new IllegalArgumentException("Planning entry not found: " + planningId));

        planning.startTask();

        Planning updatedPlanning = planningRepository.save(planning);
        log.info("Task started successfully: {}", planningId);
        return updatedPlanning;
    }

    /**
     * Complete a planning task
     * @param planningId the planning ID
     * @return updated planning entry
     */
    public Planning completeTask(@NotNull UUID planningId) {
        log.info("Completing task: {}", planningId);

        Planning planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new IllegalArgumentException("Planning entry not found: " + planningId));

        planning.completeTask();

        Planning updatedPlanning = planningRepository.save(planning);
        log.info("Task completed successfully: {}", planningId);
        return updatedPlanning;
    }

    /**
     * Update task progress
     * @param planningId the planning ID
     * @param progressPercentage new progress percentage (0-100)
     * @return updated planning entry
     */
    public Planning updateTaskProgress(@NotNull UUID planningId, @Positive int progressPercentage) {
        log.info("Updating task progress: {} to {}%", planningId, progressPercentage);

        Planning planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new IllegalArgumentException("Planning entry not found: " + planningId));

        planning.updateProgress(progressPercentage);

        Planning updatedPlanning = planningRepository.save(planning);
        log.info("Task progress updated successfully: {}", planningId);
        return updatedPlanning;
    }

    /**
     * Pause a planning task
     * @param planningId the planning ID
     * @return updated planning entry
     */
    public Planning pauseTask(@NotNull UUID planningId) {
        log.info("Pausing task: {}", planningId);

        Planning planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new IllegalArgumentException("Planning entry not found: " + planningId));

        planning.pauseTask();

        Planning updatedPlanning = planningRepository.save(planning);
        log.info("Task paused successfully: {}", planningId);
        return updatedPlanning;
    }

/**
 * Cancel a planning task
 * @param planningId the planning