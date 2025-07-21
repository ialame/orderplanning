package com.pcagrade.order.entity;

import com.pcagrade.order.util.AbstractUlidEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Order entity representing Pokemon card orders
 * Translated from Commande to Order with enhanced functionality
 */
@Entity
@Table(name = "`order`")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends AbstractUlidEntity {

    /**
     * Unique order number for tracking
     */
    @NotBlank(message = "Order number is required")
    @Size(max = 50, message = "Order number must not exceed 50 characters")
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    /**
     * Number of cards in this order
     */
    @NotNull(message = "Card count is required")
    @Positive(message = "Card count must be positive")
    @Min(value = 1, message = "Minimum 1 card per order")
    @Max(value = 10000, message = "Maximum 10000 cards per order")
    @Column(name = "card_count", nullable = false)
    private Integer cardCount;

    /**
     * Order priority level
     */
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    @Builder.Default
    private OrderPriority priority = OrderPriority.MEDIUM;

    /**
     * Total price of the order
     */
    @DecimalMin(value = "0.0", message = "Total price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    @Column(name = "total_price", precision = 10, scale = 2)
    private Double totalPrice;

    /**
     * Estimated processing time in minutes
     */
    @Min(value = 1, message = "Estimated time must be at least 1 minute")
    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;

    /**
     * Order status
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Date when the order was placed
     */
    @NotNull(message = "Order date is required")
    @Column(name = "order_date", nullable = false)
    @Builder.Default
    private LocalDate orderDate = LocalDate.now();

    /**
     * Order deadline
     */
    @Column(name = "deadline_date")
    private LocalDateTime deadlineDate;

    /**
     * When processing started
     */
    @Column(name = "processing_start_date")
    private LocalDateTime processingStartDate;

    /**
     * When processing finished
     */
    @Column(name = "processing_end_date")
    private LocalDateTime processingEndDate;

    /**
     * Number of unsealed packs (if applicable)
     */
    @Min(value = 0, message = "Unseal count cannot be negative")
    @Column(name = "unseal_count")
    private Integer unsealCount;

    /**
     * Customer or client name
     */
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    @Column(name = "customer_name", length = 100)
    private String customerName;

    /**
     * Additional notes or comments
     */
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Calculate estimated processing time based on card count
     * Default: 3 minutes per card
     */
    public void calculateEstimatedTime() {
        if (this.cardCount != null) {
            this.estimatedTimeMinutes = this.cardCount * 3; // 3 minutes per card
        }
    }

    /**
     * Check if order is overdue
     */
    public boolean isOverdue() {
        return deadlineDate != null
                && deadlineDate.isBefore(LocalDateTime.now())
                && status != OrderStatus.COMPLETED
                && status != OrderStatus.CANCELLED;
    }

    /**
     * Check if order is high priority
     */
    public boolean isHighPriority() {
        return priority == OrderPriority.HIGH;
    }

    /**
     * Check if order is ready for processing
     */
    public boolean isReadyForProcessing() {
        return status == OrderStatus.PENDING || status == OrderStatus.SCHEDULED;
    }

    /**
     * Check if order is in progress
     */
    public boolean isInProgress() {
        return status == OrderStatus.IN_PROGRESS;
    }

    /**
     * Check if order is completed
     */
    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }

    /**
     * Mark order as started
     */
    public void markAsStarted() {
        this.status = OrderStatus.IN_PROGRESS;
        this.processingStartDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Mark order as completed
     */
    public void markAsCompleted() {
        this.status = OrderStatus.COMPLETED;
        this.processingEndDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Update modification date
     */
    @PreUpdate
    protected void onUpdate() {
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Set creation date before persist
     */
    @PrePersist
    protected void onCreate() {
        if (this.creationDate == null) {
            this.creationDate = LocalDateTime.now();
        }
        if (this.modificationDate == null) {
            this.modificationDate = LocalDateTime.now();
        }
        if (this.orderDate == null) {
            this.orderDate = LocalDate.now();
        }
        if (this.priority == null) {
            this.priority = OrderPriority.MEDIUM;
        }
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
        // Calculate estimated time if not set
        if (this.estimatedTimeMinutes == null && this.cardCount != null) {
            calculateEstimatedTime();
        }
    }

    // ========== INNER ENUMS ==========

    /**
     * Order status enumeration
     */
    public enum OrderStatus {
        PENDING,      // Waiting to be processed
        SCHEDULED,    // Scheduled for processing
        IN_PROGRESS,  // Currently being processed
        COMPLETED,    // Processing completed
        CANCELLED     // Order cancelled
    }

    /**
     * Order priority enumeration
     */
    public enum OrderPriority {
        HIGH,     // 1 week - price >= 1000€
        MEDIUM,   // 2 weeks - price >= 500€
        LOW       // 4 weeks - price < 500€
    }
}