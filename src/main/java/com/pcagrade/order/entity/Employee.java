package com.pcagrade.order.entity;

import com.pcagrade.order.util.AbstractUlidEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Employee entity for managing staff members
 * Translated from Employe to Employee with correct method names for compatibility
 */
@Entity
@Table(name = "j_employee",
        indexes = {
                @Index(name = "idx_employee_email", columnList = "email", unique = true),
                @Index(name = "idx_employee_active", columnList = "active"),
                @Index(name = "idx_employee_name", columnList = "lastName, firstName"),
                @Index(name = "idx_employee_work_hours", columnList = "workHoursPerDay")
        })
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends AbstractUlidEntity {

    /**
     * Employee's last name
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * Employee's first name
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * Employee's email address (must be unique)
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Work hours per day (can be decimal for part-time employees)
     */
    @NotNull(message = "Work hours per day is required")
    @DecimalMin(value = "0.5", message = "Work hours must be at least 0.5 hours")
    @DecimalMax(value = "12.0", message = "Work hours cannot exceed 12 hours per day")
    @Column(name = "work_hours_per_day", nullable = false)
    private Double workHoursPerDay = 8.0;

    /**
     * Whether the employee is currently active
     */
    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Employee's phone number (optional)
     */
    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{0,20}$", message = "Invalid phone number format")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Employee's department
     */
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    @Column(name = "department", length = 100)
    private String department;

    /**
     * Employee's job position/title
     */
    @Size(max = 100, message = "Position must not exceed 100 characters")
    @Column(name = "position", length = 100)
    private String position;

    /**
     * Employee's hourly rate (for cost calculations)
     */
    /**
     * Employee's hourly rate (for cost calculations) - using BigDecimal for precision
     */
    @DecimalMin(value = "0.0", message = "Hourly rate cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Invalid hourly rate format")
    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    /**
     * Employee's skill level
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", length = 20)
    private SkillLevel skillLevel = SkillLevel.INTERMEDIATE;

    /**
     * Hire date
     */
    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    /**
     * Termination date (if applicable)
     */
    @Column(name = "termination_date")
    private LocalDateTime terminationDate;

    /**
     * Additional notes about the employee
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    // ========== ENUMS ==========

    /**
     * Skill levels for employees
     */
    public enum SkillLevel {
        BEGINNER,      // New to Pokemon card processing
        INTERMEDIATE,  // Moderate experience
        ADVANCED,      // High skill level
        EXPERT         // Master level
    }

    // ========== COMPATIBILITY METHODS FOR PLANNINGSERVICE ==========

    /**
     * Alternative getter for active status (compatibility with PlanningService)
     * @return true if employee is active
     */
    public Boolean getIsActive() {
        return this.active;
    }

    /**
     * Alternative setter for active status
     * @param isActive the active status
     */
    public void setIsActive(Boolean isActive) {
        this.active = isActive;
    }

    // ========== LIFECYCLE HOOKS ==========

    /**
     * Set creation date before persist
     */
    protected void onCreate() {
        if (this.hireDate == null) {
            this.hireDate = LocalDateTime.now();
        }
        if (this.active == null) {
            this.active = true;
        }
        if (this.workHoursPerDay == null) {
            this.workHoursPerDay = 8.0;
        }
        if (this.skillLevel == null) {
            this.skillLevel = SkillLevel.INTERMEDIATE;
        }
    }

    /**
     * Update modification date before update
     */
    @PreUpdate
    protected void onUpdate() {
        // This will be handled by AbstractUlidEntity
    }

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Get full name (first + last)
     * @return full name
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    /**
     * Get display name (last, first)
     * @return display name
     */
    public String getDisplayName() {
        return String.format("%s, %s", lastName, firstName);
    }

    /**
     * Calculate daily work minutes
     * @return work minutes per day
     */
    public int getDailyWorkMinutes() {
        return (int) (workHoursPerDay * 60);
    }

    /**
     * Calculate weekly work hours
     * @return work hours per week (assuming 5 work days)
     */
    public double getWeeklyWorkHours() {
        return workHoursPerDay * 5;
    }

    /**
     * Check if employee is available for work
     * @return true if active and has work hours > 0
     */
    public boolean isAvailableForWork() {
        return Boolean.TRUE.equals(active) && workHoursPerDay > 0;
    }

    /**
     * Check if employee is part-time
     * @return true if work hours < 8
     */
    public boolean isPartTime() {
        return workHoursPerDay != null && workHoursPerDay < 8.0;
    }

    /**
     * Check if employee is full-time
     * @return true if work hours >= 8
     */
    public boolean isFullTime() {
        return workHoursPerDay != null && workHoursPerDay >= 8.0;
    }

    /**
     * Get skill level display name
     * @return formatted skill level
     */
    public String getSkillLevelDisplay() {
        return skillLevel != null ? skillLevel.name().toLowerCase().replace("_", " ") : "Unknown";
    }

    /**
     * Calculate employment duration in days
     * @return days since hire date, or 0 if not hired yet
     */
    public long getEmploymentDurationDays() {
        if (hireDate == null) return 0;
        LocalDateTime endDate = terminationDate != null ? terminationDate : LocalDateTime.now();
        return java.time.Duration.between(hireDate, endDate).toDays();
    }

    /**
     * Check if employee is currently employed
     * @return true if active and not terminated
     */
    public boolean isCurrentlyEmployed() {
        return Boolean.TRUE.equals(active) && terminationDate == null;
    }

    /**
     * Activate employee
     */
    public void activate() {
        this.active = true;
        this.terminationDate = null;
    }

    /**
     * Deactivate employee
     */
    public void deactivate() {
        this.active = false;
        this.terminationDate = LocalDateTime.now();
    }

    /**
     * Update work hours with validation
     * @param newWorkHours new work hours per day
     */
    public void updateWorkHours(double newWorkHours) {
        if (newWorkHours < 0.5 || newWorkHours > 12.0) {
            throw new IllegalArgumentException("Work hours must be between 0.5 and 12.0");
        }
        this.workHoursPerDay = newWorkHours;
    }

    /**
     * Set email with validation
     * @param email the new email
     */
    public void setEmailWithValidation(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.trim().toLowerCase();
    }

    /**
     * Trim and format names
     */
    private void formatNames() {
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
    }

    // ========== TOSTRING FOR DEBUGGING ==========

    @Override
    public String toString() {
        return String.format("Employee{id=%s, name='%s', email='%s', active=%s, workHours=%.1f}",
                getId(), getFullName(), email, active, workHoursPerDay);
    }
}