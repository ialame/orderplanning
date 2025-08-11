// ========== CORRECTION COMPLÈTE EmployeeController.java ==========

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
 * REST Controller for Employee Management - English Version
 * Handles employee CRUD operations and planning-related endpoints
 */
@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
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

            // Get real employees from database
            List<Map<String, Object>> employees = employeeService.getAllActiveEmployees();

            System.out.println("✅ " + employees.size() + " employees returned from database");
            return ResponseEntity.ok(employees);

        } catch (Exception e) {
            System.err.println("❌ Error retrieving employees: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * 👥 GET ACTIVE EMPLOYEES ONLY
     * Endpoint: GET /api/employees/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveEmployees() {
        try {
            System.out.println("👥 Frontend: Getting active employees from database...");

            List<Map<String, Object>> employees = employeeService.getAllActiveEmployees();

            System.out.println("✅ " + employees.size() + " active employees returned from database");
            return ResponseEntity.ok(employees);

        } catch (Exception e) {
            System.err.println("❌ Error getting active employees: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * 💾 CREATE NEW EMPLOYEE
     * Endpoint: POST /api/employees
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> createEmployee(@RequestBody Map<String, Object> employeeData) {
        try {
            System.out.println("💾 Creating employee: " + employeeData);

            // Validate required fields
            String lastName = (String) employeeData.get("lastName");
            String firstName = (String) employeeData.get("firstName");
            String email = (String) employeeData.get("email");

            if (lastName == null || lastName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Last name is required"
                ));
            }

            if (firstName == null || firstName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
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
     * 📅 GET EMPLOYEES WITH PLANNING DATA
     * Endpoint: GET /api/employees/planning-data
     */
    @GetMapping("/planning-data")
    public ResponseEntity<List<Map<String, Object>>> getEmployeesWithPlanningData(
            @RequestParam(required = false) String date) {
        try {
            System.out.println("📅 Getting employees with planning data for date: " + date);

            String targetDate = date != null ? date : LocalDateTime.now().toLocalDate().toString();

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
                employeeData.put("active", emp.getActive());
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
     * 🧪 CREATE TEST EMPLOYEE
     * Endpoint: POST /api/employees/create-test
     */
    @PostMapping("/create-test")
    @Transactional
    public ResponseEntity<Map<String, Object>> createTestEmployee() {
        try {
            System.out.println("🧪 Creating test employee...");

            Map<String, Object> testData = new HashMap<>();
            testData.put("firstName", "John");
            testData.put("lastName", "Doe");
            testData.put("email", "john.doe@test.com");
            testData.put("workHoursPerDay", 8);

            return createEmployee(testData);

        } catch (Exception e) {
            System.err.println("❌ Error creating test employee: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugEmployees() {
        Map<String, Object> debug = new HashMap<>();

        try {
            // 1. Check both employee tables
            String sqlCheckFrench = "SHOW TABLES LIKE 'j_employee'";
            Query queryCheckFrench = entityManager.createNativeQuery(sqlCheckFrench);
            @SuppressWarnings("unchecked")
            List<Object> frenchTables = queryCheckFrench.getResultList();

            String sqlCheckEnglish = "SHOW TABLES LIKE 'employee'";
            Query queryCheckEnglish = entityManager.createNativeQuery(sqlCheckEnglish);
            @SuppressWarnings("unchecked")
            List<Object> englishTables = queryCheckEnglish.getResultList();

            debug.put("table_j_employee_exists", !frenchTables.isEmpty());
            debug.put("table_employee_exists", !englishTables.isEmpty());

            // 2. Data from French table (j_employee)
            if (!frenchTables.isEmpty()) {
                try {
                    String sqlCountFrench = "SELECT COUNT(*) FROM j_employee";
                    Query queryCountFrench = entityManager.createNativeQuery(sqlCountFrench);
                    Number countFrench = (Number) queryCountFrench.getSingleResult();
                    debug.put("j_employee_total_count", countFrench.intValue());

                    String sqlCountActiveFrench = "SELECT COUNT(*) FROM j_employee WHERE actif = 1";
                    Query queryCountActiveFrench = entityManager.createNativeQuery(sqlCountActiveFrench);
                    Number countActiveFrench = (Number) queryCountActiveFrench.getSingleResult();
                    debug.put("j_employee_active_count", countActiveFrench.intValue());

                    // Sample data from French table
                    String sqlSampleFrench = "SELECT HEX(id), prenom, nom, email, actif FROM j_employee LIMIT 5";
                    Query querySampleFrench = entityManager.createNativeQuery(sqlSampleFrench);
                    @SuppressWarnings("unchecked")
                    List<Object[]> sampleDataFrench = querySampleFrench.getResultList();

                    List<Map<String, Object>> employeesFrench = new ArrayList<>();
                    for (Object[] row : sampleDataFrench) {
                        Map<String, Object> emp = new HashMap<>();
                        emp.put("id", row[0]);
                        emp.put("firstName", row[1]); // prenom
                        emp.put("lastName", row[2]);  // nom
                        emp.put("email", row[3]);
                        emp.put("active", ((Number) row[4]).intValue() == 1);
                        employeesFrench.add(emp);
                    }
                    debug.put("j_employee_sample_data", employeesFrench);

                    // Table structure
                    String sqlDescFrench = "DESCRIBE j_employee";
                    Query queryDescFrench = entityManager.createNativeQuery(sqlDescFrench);
                    @SuppressWarnings("unchecked")
                    List<Object[]> structureFrench = queryDescFrench.getResultList();

                    List<String> columnsFrench = new ArrayList<>();
                    for (Object[] row : structureFrench) {
                        columnsFrench.add((String) row[0]);
                    }
                    debug.put("j_employee_columns", columnsFrench);

                } catch (Exception e) {
                    debug.put("j_employee_error", e.getMessage());
                }
            }

            // 3. Data from English table (employee)
            if (!englishTables.isEmpty()) {
                try {
                    String sqlCountEnglish = "SELECT COUNT(*) FROM employee";
                    Query queryCountEnglish = entityManager.createNativeQuery(sqlCountEnglish);
                    Number countEnglish = (Number) queryCountEnglish.getSingleResult();
                    debug.put("employee_total_count", countEnglish.intValue());

                    String sqlCountActiveEnglish = "SELECT COUNT(*) FROM employee WHERE active = 1";
                    Query queryCountActiveEnglish = entityManager.createNativeQuery(sqlCountActiveEnglish);
                    Number countActiveEnglish = (Number) queryCountActiveEnglish.getSingleResult();
                    debug.put("employee_active_count", countActiveEnglish.intValue());

                    // Sample data from English table
                    String sqlSampleEnglish = "SELECT HEX(id), first_name, last_name, email, active FROM employee LIMIT 5";
                    Query querySampleEnglish = entityManager.createNativeQuery(sqlSampleEnglish);
                    @SuppressWarnings("unchecked")
                    List<Object[]> sampleDataEnglish = querySampleEnglish.getResultList();

                    List<Map<String, Object>> employeesEnglish = new ArrayList<>();
                    for (Object[] row : sampleDataEnglish) {
                        Map<String, Object> emp = new HashMap<>();
                        emp.put("id", row[0]);
                        emp.put("firstName", row[1]);
                        emp.put("lastName", row[2]);
                        emp.put("email", row[3]);
                        emp.put("active", row[4]);
                        employeesEnglish.add(emp);
                    }
                    debug.put("employee_sample_data", employeesEnglish);

                } catch (Exception e) {
                    debug.put("employee_error", e.getMessage());
                }
            }

            debug.put("employee_service_available", employeeService != null);
            debug.put("timestamp", LocalDateTime.now().toString());
            debug.put("recommendation",
                    !frenchTables.isEmpty() ?
                            "Using j_employee table (French schema) - " + debug.get("j_employee_active_count") + " active employees" :
                            "Using employee table (English schema) - " + debug.get("employee_active_count") + " active employees"
            );

        } catch (Exception e) {
            debug.put("error", e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(debug);
    }

    /**
     * ✅ DIAGNOSTIC METHOD : Ajoutez à EmployeeController.java
     */
    @GetMapping("/debug-sql")
    public ResponseEntity<Map<String, Object>> debugSqlExecution() {
        Map<String, Object> debug = new HashMap<>();

        try {
            // Test direct de la requête SQL
            String sql = """
            SELECT 
                HEX(e.id) as id,
                e.prenom as first_name,
                e.nom as last_name,
                e.email,
                COALESCE(e.heures_travail_par_jour, 8) as work_hours_per_day,
                COALESCE(e.actif, 1) as active,
                e.date_creation as creation_date
            FROM j_employee e
            WHERE COALESCE(e.actif, 1) = 1
            ORDER BY e.nom, e.prenom
        """;

            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            debug.put("sql_success", true);
            debug.put("sql_result_count", results.size());
            debug.put("sql_query", sql);

            // Essayer de traiter la première ligne pour voir s'il y a des erreurs de mapping
            if (!results.isEmpty()) {
                Object[] firstRow = results.get(0);
                debug.put("first_row_data", Arrays.toString(firstRow));

                try {
                    Map<String, Object> testEmployee = new HashMap<>();
                    testEmployee.put("id", (String) firstRow[0]);
                    testEmployee.put("firstName", (String) firstRow[1]);
                    testEmployee.put("lastName", (String) firstRow[2]);
                    testEmployee.put("email", (String) firstRow[3]);

                    debug.put("mapping_success", true);
                    debug.put("sample_employee", testEmployee);

                } catch (Exception mappingError) {
                    debug.put("mapping_error", mappingError.getMessage());
                    debug.put("mapping_success", false);
                }
            }

            // Test via service
            List<Map<String, Object>> serviceResult = employeeService.getAllActiveEmployees();
            debug.put("service_result_count", serviceResult.size());

        } catch (Exception e) {
            debug.put("sql_success", false);
            debug.put("sql_error", e.getMessage());
        }

        return ResponseEntity.ok(debug);
    }
}