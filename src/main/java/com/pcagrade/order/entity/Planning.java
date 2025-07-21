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
    @Positive(message = "Duration must be positive")
    @Min(value = 1, message = "Minimum duration is 1 minute")
    @Max(value = 1440, message = "Maximum duration is 1440 minutes (24 hours)")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /**
     * Whether this task has been completed
     */
    @NotNull(message = "Completed status is required")
    @Column(name = "completed", nullable = false)
    @Builder.Default
    private Boolean completed = false;

    /**
     * Priority level of this planning entry
     */
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    @Builder.Default
    private PlanningPriority priority = PlanningPriority.MEDIUM;

    /**
     * Current status of this planning entry
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private PlanningStatus status = PlanningStatus.SCHEDULED;

    /**
     * Progress percentage (0-100)
     */
    @Min(value = 0, message = "Progress cannot be negative")
    @Max(value = 100, message = "Progress cannot exceed 100%")
    @Column(name = "progress_percentage")
    @Builder.Default
    private Integer progressPercentage = 0;

    /**
     * Actual start time when work began
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * Actual end time when work finished
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * Calculated end time based on start time and duration
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Number of cards to process in this task
     */
    @Positive(message = "Card count cannot be negative")
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
    @Builder.Default
    private LocalDateTime creationDate = LocalDateTime.now();

    /**
     * Record last modification timestamp
     */
    @Column(name = "modification_date", nullable = false)
    @Builder.Default
    private LocalDateTime modificationDate = LocalDateTime.now();

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

        // Calculate end time automatically
        this.endTime = startTime.plusMinutes(durationMinutes);
    }

    // ========== UTILITY METHODS ==========

    /**
     * Calculate and set end time based on start time and duration
     */
    @PrePersist
    @PreUpdate
    public void calculateEndTime() {
        if (startTime != null && durationMinutes != null) {
            this.endTime = startTime.plusMinutes(durationMinutes);
        }
        if (this.creationDate == null) {
            this.creationDate = LocalDateTime.now();
        }
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Check if this planning entry is overdue
     */
    public boolean isOverdue() {
        if (completed || status == PlanningStatus.COMPLETED) {
            return false;
        }
        LocalDateTime plannedDateTime = LocalDateTime.of(planningDate, endTime);
        return LocalDateTime.now().isAfter(plannedDateTime);
    }

    /**
     * Get total planned duration in hours as a formatted string
     */
    public String getFormattedDuration() {
        if (durationMinutes == null) return "0h00";
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return String.format("%dh%02d", hours, minutes);
    }

    /**
     * Get actual duration if both start and end times are set
     */
    public Integer getActualDurationMinutes() {
        if (actualStartTime != null && actualEndTime != null) {
            return (int) java.time.Duration.between(actualStartTime, actualEndTime).toMinutes();
        }
        return null;
    }

    /**
     * Mark this planning entry as completed
     */
    public void markAsCompleted() {
        this.completed = true;
        this.status = PlanningStatus.COMPLETED;
        this.progressPercentage = 100;
        if (this.actualEndTime == null) {
            this.actualEndTime = LocalDateTime.now();
        }
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Start working on this planning entry
     */
    public void startWork() {
        this.status = PlanningStatus.IN_PROGRESS;
        this.actualStartTime = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Pause work on this planning entry
     */
    public void pauseWork() {
        this.status = PlanningStatus.PAUSED;
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Cancel this planning entry
     */
    public void cancel() {
        this.status = PlanningStatus.CANCELLED;
        this.modificationDate = LocalDateTime.now();
    }
}