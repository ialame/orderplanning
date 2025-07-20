package com.pcagrade.order.entity;

import com.pcagrade.order.util.AbstractUlidEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Employee entity for managing staff members
 * Translated from Employe to Employee with UUID instead of ULID
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
    @Column(name = "work_hours_per_day", nullable = false, precision = 3, scale = 1)
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
     * Employee's department or team
     */
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    @Column(name = "department", length = 100)
    private String department;

    /**
     * Employee's job title or position
     */
    @Size(max = 100, message = "Position must not exceed 100 characters")
    @Column(name = "position", length = 100)
    private String position;

    /**
     * Employee's hourly rate (optional, for cost calculations)
     */
    @DecimalMin(value = "0.0", message = "Hourly rate cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Invalid hourly rate format")
    @Column(name = "hourly_rate", precision = 7, scale = 2)
    private Double hourlyRate;

    /**
     * Employee's skill level (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", length = 20)
    private SkillLevel skillLevel = SkillLevel.INTERMEDIATE;

    /**
     * Date when employee was hired
     */
    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    /**
     * Date when employee left (null if still active)
     */
    @Column(name = "termination_date")
    private LocalDateTime terminationDate;

    /**
     * Additional notes about the employee
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
     * Skill level enumeration
     */
    public enum SkillLevel {
        BEGINNER,     // New employee, needs training
        INTERMEDIATE, // Standard level, can work independently
        ADVANCED,     // Experienced, can handle complex tasks
        EXPERT        // Master level, can train others
    }

    // ========== CONSTRUCTORS ==========

    /**
     * Constructor for creating a basic employee
     */
    public Employee(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.workHoursPerDay = 8.0;
        this.active = true;
        this.skillLevel = SkillLevel.INTERMEDIATE;
        this.hireDate = LocalDateTime.now();
        this.creationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
    }

    /**
     * Constructor with work hours
     */
    public Employee(String firstName, String lastName, String email, Double workHoursPerDay) {
        this(firstName, lastName, email);
        this.workHoursPerDay = workHoursPerDay;
    }

    // ========== UTILITY METHODS ==========

    /**
     * Get employee's full name
     * @return formatted full name (FirstName LastName)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Get employee's display name for UI
     * @return formatted display name (LastName, FirstName)
     */
    public String getDisplayName() {
        return lastName + ", " + firstName;
    }

    /**
     * Get employee's initials
     * @return initials (FL)
     */
    public String getInitials() {
        String firstInitial = (firstName != null && !firstName.isEmpty()) ?
                firstName.substring(0, 1).toUpperCase() : "";
        String lastInitial = (lastName != null && !lastName.isEmpty()) ?
                lastName.substring(0, 1).toUpperCase() : "";
        return firstInitial + lastInitial;
    }

    /**
     * Calculate total work minutes per day
     * @return work minutes per day
     */
    public Integer getWorkMinutesPerDay() {
        return (workHoursPerDay != null) ? (int) (workHoursPerDay * 60) : 480; // Default 8 hours
    }

    /**
     * Check if employee is part-time (less than 8 hours)
     * @return true if part-time
     */
    public boolean isPartTime() {
        return workHoursPerDay != null && workHoursPerDay < 8.0;
    }

    /**
     * Check if employee is full-time (8+ hours)
     * @return true if full-time
     */
    public boolean isFullTime() {
        return workHoursPerDay != null && workHoursPerDay >= 8.0;
    }

    /**
     * Get work capacity category
     * @return capacity level
     */
    public String getWorkCapacity() {
        if (workHoursPerDay == null) return "UNKNOWN";
        if (workHoursPerDay >= 8.0) return "HIGH";
        if (workHoursPerDay >= 6.0) return "MEDIUM";
        return "LOW";
    }

    /**
     * Calculate daily cost (hourly rate * work hours)
     * @return daily cost or null if hourly rate not set
     */
    public Double getDailyCost() {
        if (hourlyRate != null && workHoursPerDay != null) {
            return hourlyRate * workHoursPerDay;
        }
        return null;
    }

    /**
     * Check if employee is currently employed
     * @return true if active and not terminated
     */
    public boolean isCurrentlyEmployed() {
        return active != null && active && terminationDate == null;
    }

    /**
     * Get employment duration in days
     * @return days since hire date
     */
    public Long getEmploymentDurationDays() {
        if (hireDate == null) return null;
        LocalDateTime endDate = (terminationDate != null) ? terminationDate : LocalDateTime.now();
        return java.time.Duration.between(hireDate, endDate).toDays();
    }

    // ========== JPA LIFECYCLE CALLBACKS ==========

    /**
     * Set timestamps before persisting
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
        if (hireDate == null) {
            hireDate = now;
        }

        // Validate business rules
        validateBusinessRules();
    }

    /**
     * Update modification timestamp before updating
     */
    @PreUpdate
    protected void onUpdate() {
        modificationDate = LocalDateTime.now();

        // Validate business rules
        validateBusinessRules();
    }

    /**
     * Validate business rules
     */
    private void validateBusinessRules() {
        // If employee is inactive, ensure termination date is set
        if (active != null && !active && terminationDate == null) {
            terminationDate = LocalDateTime.now();
        }

        // If termination date is set, mark as inactive
        if (terminationDate != null && (active == null || active)) {
            active = false;
        }

        // Ensure email is lowercase
        if (email != null) {
            email = email.toLowerCase().trim();
        }

        // Trim names
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
    }

    // ========== BUILDER PATTERN METHODS ==========

    /**
     * Create a new employee builder
     * @return new employee builder
     */
    public static EmployeeBuilder builder() {
        return new EmployeeBuilder();
    }

    /**
     * Custom builder for Employee
     */
    public static class EmployeeBuilder {
        private String firstName;
        private String lastName;
        private String email;
        private Double workHoursPerDay = 8.0;
        private Boolean active = true;
        private String phoneNumber;
        private String department;
        private String position;
        private Double hourlyRate;
        private SkillLevel skillLevel = SkillLevel.INTERMEDIATE;
        private String notes;

        public EmployeeBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public EmployeeBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public EmployeeBuilder email(String email) {
            this.email = email;
            return this;
        }

        public EmployeeBuilder workHours(Double workHoursPerDay) {
            this.workHoursPerDay = workHoursPerDay;
            return this;
        }

        public EmployeeBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public EmployeeBuilder phone(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public EmployeeBuilder department(String department) {
            this.department = department;
            return this;
        }

        public EmployeeBuilder position(String position) {
            this.position = position;
            return this;
        }

        public EmployeeBuilder hourlyRate(Double hourlyRate) {
            this.hourlyRate = hourlyRate;
            return this;
        }

        public EmployeeBuilder skillLevel(SkillLevel skillLevel) {
            this.skillLevel = skillLevel;
            return this;
        }

        public EmployeeBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Employee build() {
            Employee employee = new Employee();
            employee.firstName = this.firstName;
            employee.lastName = this.lastName;
            employee.email = this.email;
            employee.workHoursPerDay = this.workHoursPerDay;
            employee.active = this.active;
            employee.phoneNumber = this.phoneNumber;
            employee.department = this.department;
            employee.position = this.position;
            employee.hourlyRate = this.hourlyRate;
            employee.skillLevel = this.skillLevel;
            employee.notes = this.notes;
            employee.hireDate = LocalDateTime.now();
            employee.creationDate = LocalDateTime.now();
            employee.modificationDate = LocalDateTime.now();
            return employee;
        }
    }
}
