// ========== ENGLISH VERSION: EmployeeController.java ==========
// src/main/java/com/pcagrade/order/controller/EmployeeController.java

package com.pcagrade.order.controller;

import com.pcagrade.order.entity.Employee;
import com.pcagrade.order.service.EmployeeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST Controller for Employee Management
 * Handles employee CRUD operations and planning-related endpoints
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EmployeeService employeeService;

    /**
     * 👥 GET ALL EMPLOYEES FOR FRONTEND
     * Endpoint: GET /api/employees
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllEmployees() {
        try {
            System.out.println("👥 Frontend: Retrieving employees list...");

            // Try to get real employees from database first
            List<Map<String, Object>> employees = employeeService.getAllActiveEmployees();

            if (employees.isEmpty()) {
                System.out.println("⚠️ No employees in database - returning test employees");
                employees = createTestEmployees();
            }

            System.out.println("✅ " + employees.size() + " employees returned");
            return ResponseEntity.ok(employees);

        } catch (Exception e) {
            System.err.println("❌ Error retrieving employees: " + e.getMessage());
            e.printStackTrace();
            // Fallback to test employees in case of error
            return ResponseEntity.ok(createTestEmployees());
        }
    }

    /**
     * 👥 GET EMPLOYEES FOR FRONTEND (alternative endpoint)
     * Endpoint: GET /api/employees/frontend/list
     */
    @GetMapping("/frontend/list")
    public ResponseEntity<List<Map<String, Object>>> getEmployeesFrontend() {
        return getAllEmployees(); // Use the same logic
    }

    /**
     * 💾 CREATE NEW EMPLOYEE
     * Endpoint: POST /api/employees
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEmployee(@RequestBody Map<String, Object> employeeData) {
        try {
            System.out.println("💾 Creating employee: " + employeeData);

            // Validate required fields
            String lastName = (String) employeeData.get("lastName");
            String firstName = (String) employeeData.get("firstName");
            String email = (String) employeeData.get("email");

            if (lastName == null || lastName.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Last name is required"
                ));
            }

            if (firstName == null || firstName.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "First name is required"
                ));
            }

            // Create Employee entity
            Employee newEmployee = new Employee();
            newEmployee.setLastName(lastName.trim());
            newEmployee.setFirstName(firstName.trim());
            newEmployee.setEmail(email != null ? email.trim() : "");

            // Handle work hours per day
            Object hoursObj = employeeData.get("workHoursPerDay");
            if (hoursObj instanceof Number) {
                newEmployee.setWorkHoursPerDay(((Number) hoursObj).intValue());
            } else {
                newEmployee.setWorkHoursPerDay(8); // Default
            }

            newEmployee.setActive(true);
            newEmployee.setCreationDate(LocalDateTime.now());
            newEmployee.setModificationDate(LocalDateTime.now());

            // Save via service
            Employee savedEmployee = employeeService.createEmployee(newEmployee);

            System.out.println("✅ Employee saved with ID: " + savedEmployee.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee created successfully");
            response.put("id", savedEmployee.getId().toString());
            response.put("fullName", firstName + " " + lastName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error creating employee: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error creating employee: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 💾 CREATE EMPLOYEE (alternative endpoint for frontend compatibility)
     * Endpoint: POST /api/employees/frontend/create
     */
    @PostMapping("/frontend/create")
    public ResponseEntity<Map<String, Object>> createEmployeeFrontend(@RequestBody Map<String, Object> employeeData) {
        return createEmployee(employeeData);
    }

    /**
     * 📅 GET EMPLOYEES WITH PLANNING DATA
     * Endpoint: GET /api/employees/planning
     */
    @GetMapping("/planning")
    public ResponseEntity<List<Map<String, Object>>> getEmployeesWithPlanning(
            @RequestParam(required = false) String date) {
        try {
            System.out.println("📅 Getting employees with planning for date: " + date);

            String targetDate = date != null ? date : LocalDateTime.now().toLocalDate().toString();

            // Try to get planning data from service
            List<Map<String, Object>> employeesWithPlanning = employeeService.getEmployeesWithPlanningData(targetDate);

            if (employeesWithPlanning.isEmpty()) {
                // Fallback: convert regular employees to planning format
                List<Map<String, Object>> employees = employeeService.getAllActiveEmployees();
                employeesWithPlanning = employees.stream().map(emp -> {
                    Map<String, Object> planningEmp = new HashMap<>(emp);
                    planningEmp.put("name", emp.get("fullName"));
                    planningEmp.put("totalMinutes", 0);
                    planningEmp.put("maxMinutes", ((Number) emp.getOrDefault("workHoursPerDay", 8)).intValue() * 60);
                    planningEmp.put("status", "available");
                    planningEmp.put("taskCount", 0);
                    planningEmp.put("cardCount", 0);
                    return planningEmp;
                }).toList();
            }

            System.out.println("✅ " + employeesWithPlanning.size() + " employees with planning returned");
            return ResponseEntity.ok(employeesWithPlanning);

        } catch (Exception e) {
            System.err.println("❌ Error getting employees with planning: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * 👤 GET EMPLOYEE BY ID
     * Endpoint: GET /api/employees/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEmployeeById(@PathVariable String id) {
        try {
            System.out.println("👤 Getting employee by ID: " + id);

            Optional<Employee> employee = employeeService.findById(id);

            if (employee.isPresent()) {
                Employee emp = employee.get();
                Map<String, Object> employeeData = new HashMap<>();
                employeeData.put("id", emp.getId().toString());
                employeeData.put("firstName", emp.getFirstName());
                employeeData.put("lastName", emp.getLastName());
                employeeData.put("email", emp.getEmail());
                employeeData.put("workHoursPerDay", emp.getWorkHoursPerDay());
                employeeData.put("active", emp.isActive());
                employeeData.put("creationDate", emp.getCreationDate());
                employeeData.put("fullName", emp.getFirstName() + " " + emp.getLastName());

                return ResponseEntity.ok(employeeData);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Error getting employee by ID: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✏️ UPDATE EMPLOYEE
     * Endpoint: PUT /api/employees/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEmployee(
            @PathVariable String id,
            @RequestBody Map<String, Object> employeeData) {
        try {
            System.out.println("✏️ Updating employee: " + id);

            Optional<Employee> existingEmployee = employeeService.findById(id);

            if (!existingEmployee.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Employee not found"
                ));
            }

            Employee employee = existingEmployee.get();

            // Update fields if provided
            if (employeeData.containsKey("firstName")) {
                employee.setFirstName((String) employeeData.get("firstName"));
            }
            if (employeeData.containsKey("lastName")) {
                employee.setLastName((String) employeeData.get("lastName"));
            }
            if (employeeData.containsKey("email")) {
                employee.setEmail((String) employeeData.get("email"));
            }
            if (employeeData.containsKey("workHoursPerDay")) {
                employee.setWorkHoursPerDay(((Number) employeeData.get("workHoursPerDay")).intValue());
            }
            if (employeeData.containsKey("active")) {
                employee.setActive((Boolean) employeeData.get("active"));
            }

            employee.setModificationDate(LocalDateTime.now());

            Employee updatedEmployee = employeeService.updateEmployee(employee);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee updated successfully");
            response.put("id", updatedEmployee.getId().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error updating employee: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating employee: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 🗑️ DELETE EMPLOYEE
     * Endpoint: DELETE /api/employees/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmployee(@PathVariable String id) {
        try {
            System.out.println("🗑️ Deleting employee: " + id);

            employeeService.deleteEmployee(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error deleting employee: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error deleting employee: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 🔍 DEBUG ENDPOINT FOR TROUBLESHOOTING
     * Endpoint: GET /api/employees/debug
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugEmployees() {
        Map<String, Object> debug = new HashMap<>();

        try {
            // 1. Check if employee table exists
            String sqlCheckTable = "SHOW TABLES LIKE 'employee'";
            Query queryCheck = entityManager.createNativeQuery(sqlCheckTable);
            @SuppressWarnings("unchecked")
            List<Object> tables = queryCheck.getResultList();

            debug.put("table_employee_exists", !tables.isEmpty());

            if (!tables.isEmpty()) {
                // 2. Count employees
                String sqlCount = "SELECT COUNT(*) FROM employee";
                Query queryCount = entityManager.createNativeQuery(sqlCount);
                Number count = (Number) queryCount.getSingleResult();
                debug.put("total_employees_count", count.intValue());

                // 3. Count active employees
                String sqlCountActive = "SELECT COUNT(*) FROM employee WHERE active = 1";
                Query queryCountActive = entityManager.createNativeQuery(sqlCountActive);
                Number countActive = (Number) queryCountActive.getSingleResult();
                debug.put("active_employees_count", countActive.intValue());

                // 4. Table structure
                String sqlDesc = "DESCRIBE employee";
                Query queryDesc = entityManager.createNativeQuery(sqlDesc);
                @SuppressWarnings("unchecked")
                List<Object[]> structure = queryDesc.getResultList();

                List<String> columns = new ArrayList<>();
                for (Object[] row : structure) {
                    columns.add((String) row[0]);
                }
                debug.put("table_columns", columns);
            }

            debug.put("employee_service_available", employeeService != null);
            debug.put("timestamp", LocalDateTime.now().toString());

        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }

        return ResponseEntity.ok(debug);
    }

    /**
     * 🔧 INITIALIZE EMPLOYEE TABLE
     * Endpoint: POST /api/employees/init-table
     */
    @PostMapping("/init-table")
    @Transactional
    public ResponseEntity<Map<String, Object>> initializeEmployeeTable() {
        try {
            System.out.println("🔧 Initializing employee table...");

            String createTableSql = """
                CREATE TABLE IF NOT EXISTS employee (
                    id BINARY(16) NOT NULL PRIMARY KEY,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    email VARCHAR(150),
                    work_hours_per_day INT DEFAULT 8,
                    active BOOLEAN DEFAULT TRUE,
                    creation_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                    modification_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_employee_active (active),
                    INDEX idx_employee_name (last_name, first_name)
                )
                """;

            Query query = entityManager.createNativeQuery(createTableSql);
            query.executeUpdate();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee table created/verified successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error creating table: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error creating table: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 🧪 CREATE TEST EMPLOYEES (FALLBACK)
     */
    private List<Map<String, Object>> createTestEmployees() {
        List<Map<String, Object>> employees = new ArrayList<>();

        String[][] employeeData = {
                {"John", "Smith", "john.smith@test.com", "8"},
                {"Mary", "Johnson", "mary.johnson@test.com", "7"},
                {"David", "Wilson", "david.wilson@test.com", "8"},
                {"Sarah", "Brown", "sarah.brown@test.com", "6"}
        };

        for (int i = 0; i < employeeData.length; i++) {
            Map<String, Object> employee = new HashMap<>();
            employee.put("id", "test-" + (i + 1));
            employee.put("firstName", employeeData[i][0]);
            employee.put("lastName", employeeData[i][1]);
            employee.put("email", employeeData[i][2]);
            employee.put("workHoursPerDay", Integer.parseInt(employeeData[i][3]));
            employee.put("active", true);
            employee.put("creationDate", LocalDateTime.now().toString());
            employee.put("fullName", employeeData[i][0] + " " + employeeData[i][1]);
            employee.put("available", true);
            employee.put("currentLoad", 0);
            employees.add(employee);
        }

        return employees;
    }
}