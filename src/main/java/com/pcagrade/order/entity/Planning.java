package com.pcagrade.order.entity;

import com.pcagrade.order.util.AbstractUlidEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Planning entity for managing work schedules and task assignments
 * Translated from Planification to Planning with UUID instead of ULID
 */
@Entity
@Table(name = "j_planning",
        indexes = {
                @Index(name = "idx_planning_employee_date", columnList = "employeeId, planningDate"),
                @Index(name = "idx_planning_order", columnList = "orderId"),
                @Index(name = "idx_planning_date", columnList = "planningDate"),
                @Index(name = "idx_planning_completed", columnList = "completed"),
                @Index(name = "idx_planning_start_time", columnList = "startTime"),
                @Index(name = "idx_planning_employee_time", columnList = "employeeId, planningDate, startTime")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Planning extends AbstractUlidEntity {

    /**
     * ID of the order this planning entry is for
     */
    @NotNull(message = "Order ID is required")
    @Column(name = "order_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID orderId;

    /**
     * ID of the employee assigned to this task
     */
    @NotNull(message = "Employee ID is required")
    @Column(name = "employee_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID employeeId;

    /**
     * Date when this task is planned
     */
    @NotNull(message = "Planning date is required")
    @Column(name = "planning_date", nullable = false)
    private LocalDate planningDate;

    /**
     * Start time of the task
     */
    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * Duration of the task in minutes
     */
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 720, message = "Duration cannot exceed 12 hours (720 minutes)")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /**
     * Whether this task has been completed
     */
    @NotNull(message = "Completion status is required")
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    /**
     * Priority level of this planning entry
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private PlanningPriority priority = PlanningPriority.MEDIUM;

    /**
     * Status of this planning entry
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PlanningStatus status = PlanningStatus.SCHEDULED;

    /**
     * Actual start time (when task actually began)
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * Actual end time (when task actually finished)
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * Estimated end time (calculated from start time + duration)
     */
    @Column(name = "estimated_end_time")
    private LocalTime estimatedEndTime;

    /**
     * Progress percentage (0-100)
     */
    @Min(value = 0, message = "Progress cannot be negative")
    @Max(value = 100, message = "Progress cannot exceed 100%")
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    /**
     * Number of cards to process in this planning entry
     */
    @Min(value = 0, message = "Card count cannot be negative")
    @Column(name = "card_count")
    private Integer cardCount;

    /**
     * Estimated cost for this task
     */
    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid cost format")
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private Double estimatedCost;

    /**
     * Actual cost for this task
     */
    @DecimalMin(value = "0.0", message = "Actual cost cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid actual cost format")
    @Column(name = "actual_cost", precision = 10, scale = 2)
    private Double actualCost;

    /**
     * Comments or notes about this planning entry
     */
    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    /**
     * Record creation timestamp
     */
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    /**
     * Record last modification timestamp
     */
    @Column(name = "modification_date", nullable = false)
    private LocalDateTime modificationDate;

    /**
     * Who created this planning entry
     */
    @Size(max = 100, message = "Created by field must not exceed 100 characters")
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * Who last modified this planning entry
     */
    @Size(max = 100, message = "Modified by field must not exceed 100 characters")
    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    // ========== RELATIONSHIPS (OPTIONAL) ==========

    /**
     * Reference to the Order entity (lazy loaded to avoid performance issues)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    /**
     * Reference to the Employee entity (lazy loaded to avoid performance issues)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    // ========== ENUMS ==========

    /**
     * Priority levels for planning entries
     */
    public enum PlanningPriority {
        URGENT,    // Must be done immediately
        HIGH,      // Important, should be done soon
        MEDIUM,    // Normal priority
        LOW        // Can be delayed if needed
    }

    /**
     * Status of planning entries
     */
    public enum PlanningStatus {
        SCHEDULED,   // Planned but not started
        IN_PROGRESS, // Currently being worked on
        PAUSED,      // Temporarily stopped
        COMPLETED,   // Finished successfully
        CANCELLED,   // Cancelled before completion
        OVERDUE      // Past due date and not completed
    }

    // ========== CONSTRUCTORS ==========

    /**
     * Constructor for basic planning entry
     */
    public Planning(UUID orderId, UUID employeeId, LocalDate planningDate,
                    LocalTime startTime, Integer durationMinutes) {
        this.orderId = orderId;
        this.employeeId = employeeId;
        this.planningDate = planningDate;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.completed = false;
        this.priority = PlanningPriority.MEDIUM;
        this.status = PlanningStatus.SCHEDULED;
        this.progressPercentage = 0;
        this.creationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();

        // Calculate estimated end time
        this.estimatedEndTime = calculateEndTime();
    }

    // ========== UTILITY METHODS ==========

    /**
     * Calculate estimated end time based on start time and duration
     * @return estimated end time
     */
    public LocalTime calculateEndTime() {
        if (startTime != null && durationMinutes != null) {
            return startTime.plusMinutes(durationMinutes);
        }
        return null;
    }

    /**
     * Get actual duration in minutes (if task is completed)
     * @return actual duration or null if not completed
     */
    public Integer getActualDurationMinutes() {
        if (actualStartTime != null && actualEndTime != null) {
            return (int) java.time.Duration.between(actualStartTime, actualEndTime).toMinutes();
        }
        return null;
    }

    /**
     * Check if this planning entry is overdue
     * @return true if past the planned date and not completed
     */
    public boolean isOverdue() {
        return planningDate != null
                && planningDate.isBefore(LocalDate.now())
                && !Boolean.TRUE.equals(completed);
    }

    /**
     * Check if this planning entry is for today
     * @return true if planned for today
     */
    public boolean isToday() {
        return planningDate != null && planningDate.equals(LocalDate.now());
    }

    /**
     * Check if this planning entry is in the future
     * @return true if planned for future date
     */
    public boolean isFuture() {
        return planningDate != null && planningDate.isAfter(LocalDate.now());
    }

    /**
     * Get formatted time range (e.g., "09:00 - 10:30")
     * @return formatted time range
     */
    public String getTimeRange() {
        if (startTime != null) {
            LocalTime endTime = (estimatedEndTime != null) ? estimatedEndTime : calculateEndTime();
            if (endTime != null) {
                return String.format("%s - %s", startTime.toString(), endTime.toString());
            }
            return startTime.toString();
        }
        return "Unknown";
    }

    /**
     * Get duration in hours and minutes format
     * @return formatted duration (e.g., "2h 30m")
     */
    public String getFormattedDuration() {
        if (durationMinutes != null) {
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            if (hours > 0 && minutes > 0) {
                return String.format("%dh %dm", hours, minutes);
            } else if (hours > 0) {
                return String.format("%dh", hours);
            } else {
                return String.format("%dm", minutes);
            }
        }
        return "Unknown";
    }

    /**
     * Calculate efficiency percentage (estimated vs actual time)
     * @return efficiency percentage or null if not applicable
     */
    public Double getEfficiencyPercentage() {
        Integer actualDuration = getActualDurationMinutes();
        if (durationMinutes != null && actualDuration != null && actualDuration > 0) {
            return (durationMinutes.doubleValue() / actualDuration.doubleValue()) * 100;
        }
        return null;
    }

    /**
     * Check if there's a time conflict with another planning entry
     * @param other another planning entry
     * @return true if there's a time conflict
     */
    public boolean hasTimeConflictWith(Planning other) {
        if (other == null || !this.employeeId.equals(other.employeeId)
                || !this.planningDate.equals(other.planningDate)) {
            return false;
        }

        LocalTime thisEnd = this.calculateEndTime();
        LocalTime otherEnd = other.calculateEndTime();

        if (thisEnd == null || otherEnd == null) {
            return false;
        }

        // Check for overlap
        return (this.startTime.isBefore(otherEnd) && thisEnd.isAfter(other.startTime));
    }

    /**
     * Start the task (set actual start time and update status)
     */
    public void startTask() {
        this.actualStartTime = LocalDateTime.now();
        this.status = PlanningStatus.IN_PROGRESS;
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Complete the task (set actual end time, mark as completed)
     */
    public void completeTask() {
        this.actualEndTime = LocalDateTime.now();
        this.completed = true;
        this.status = PlanningStatus.COMPLETED;
        this.progressPercentage = 100;
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Pause the task
     */
    public void pauseTask() {
        this.status = PlanningStatus.PAUSED;
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Cancel the task
     */
    public void cancelTask() {
        this.status = PlanningStatus.CANCELLED;
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Update progress percentage
     * @param progress new progress percentage (0-100)
     */
    public void updateProgress(Integer progress) {
        if (progress != null && progress >= 0 && progress <= 100) {
            this.progressPercentage = progress;
            this.modificationDate = LocalDateTime.now();

            // Auto-complete if progress reaches 100%
            if (progress == 100 && !Boolean.TRUE.equals(completed)) {
                completeTask();
            }
        }
    }

    // ========== JPA LIFECYCLE CALLBACKS ==========

    /**
     * Set timestamps and calculate values before persisting
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (creationDate == null) {
            creationDate = now;
        }
        if (modificationDate == null) {
            modificationDate = now;
        }

        // Calculate estimated end time
        estimatedEndTime = calculateEndTime();

        // Update status based on completion
        updateStatusFromCompletion();

        // Validate business rules
        validateBusinessRules();
    }

    /**
     * Update modification timestamp and recalculate values before updating
     */
    @PreUpdate
    protected void onUpdate() {
        modificationDate = LocalDateTime.now();

        // Recalculate estimated end time
        estimatedEndTime = calculateEndTime();

        // Update status based on completion
        updateStatusFromCompletion();

        // Check if overdue
        if (isOverdue() && status != PlanningStatus.COMPLETED && status != PlanningStatus.CANCELLED) {
            status = PlanningStatus.OVERDUE;
        }

        // Validate business rules
        validateBusinessRules();
    }

    /**
     * Update status based on completion flag
     */
    private void updateStatusFromCompletion() {
        if (Boolean.TRUE.equals(completed)) {
            if (status != PlanningStatus.COMPLETED) {
                status = PlanningStatus.COMPLETED;
                progressPercentage = 100;
            }
        }
    }

    /**
     * Validate business rules
     */
    private void validateBusinessRules() {
        // Ensure planning date is not too far in the past
        if (planningDate != null && planningDate.isBefore(LocalDate.now().minusDays(365))) {
            throw new IllegalArgumentException("Planning date cannot be more than 1 year in the past");
        }

        // Ensure start time is reasonable (between 00:00 and 23:59)
        if (startTime != null && (startTime.isBefore(LocalTime.of(0, 0)) || startTime.isAfter(LocalTime.of(23, 59)))) {
            throw new IllegalArgumentException("Start time must be between 00:00 and 23:59");
        }

        // Ensure actual end time is after actual start time
        if (actualStartTime != null && actualEndTime != null && actualEndTime.isBefore(actualStartTime)) {
            throw new IllegalArgumentException("Actual end time cannot be before actual start time");
        }

        // Ensure progress percentage is valid
        if (progressPercentage != null && (progressPercentage < 0 || progressPercentage > 100)) {
            progressPercentage = Math.max(0, Math.min(100, progressPercentage));
        }
    }
}
