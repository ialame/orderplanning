package com.pcagrade.order.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import com.pcagrade.order.service.EmployeeService;
import com.pcagrade.order.service.OrderService;
import com.pcagrade.order.service.PlanningService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planning")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PlanningController {

    @Autowired
    private EntityManager entityManager;

    // Reuse existing French services that work
    @Autowired
    private OrderService orderService;

    @Autowired
    private EmployeeService employeeService;

    /**
     * Debug endpoint - English version of French diagnostic
     */
    @GetMapping("/debug-real")
    public ResponseEntity<Map<String, Object>> debugReal() {
        Map<String, Object> debug = new HashMap<>();

        try {
            // Reuse working French service logic
            List<Map<String, Object>> activeEmployees = employeeService.getTousEmployesActifs();
            debug.put("activeEmployees", activeEmployees.size());

            // Count orders since June 2025 as requested
            String sqlCount = "SELECT COUNT(*) FROM `order` WHERE status IN (1, 2) AND date >= '2025-06-01'";
            Query countQuery = entityManager.createNativeQuery(sqlCount);
            Long availableOrders = ((Number) countQuery.getSingleResult()).longValue();
            debug.put("availableOrders", availableOrders);

            // Sample orders with English field names
            String sqlOrders = """
                SELECT HEX(o.id), o.num_commande, o.priority,
                       COUNT(cco.card_certification_id) as card_count
                FROM `order` o
                LEFT JOIN card_certification_order cco ON o.id = cco.order_id
                WHERE o.status IN (1, 2) AND o.date >= '2025-06-01'
                GROUP BY o.id
                ORDER BY o.date ASC
                LIMIT 5
            """;

            Query ordersQuery = entityManager.createNativeQuery(sqlOrders);
            @SuppressWarnings("unchecked")
            List<Object[]> sampleOrders = ordersQuery.getResultList();

            List<Map<String, Object>> ordersData = new ArrayList<>();
            for (Object[] row : sampleOrders) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", row[0]);
                order.put("orderNumber", row[1]);
                order.put("priority", row[2]);
                order.put("cardCount", row[3]);
                ordersData.add(order);
            }

            debug.put("sampleOrders", ordersData);
            debug.put("status", "SUCCESS");
            debug.put("message", "English planning system using j_planning table");

            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            debug.put("error", e.getMessage());
            debug.put("status", "ERROR");
            return ResponseEntity.ok(debug);
        }
    }

    /**
     * Generate planning - Translated from French system
     */
    @PostMapping("/generate")
    @Transactional
    public ResponseEntity<Map<String, Object>> generatePlanning(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> result = new HashMap<>();

        try {
            // English parameter names
            String startDate = (String) request.get("startDate");
            Integer numberOfEmployees = (Integer) request.get("numberOfEmployees");
            Integer timePerCard = (Integer) request.get("timePerCard");

            // Reuse existing French services that work
            List<Map<String, Object>> employees = employeeService.getTousEmployesActifs();
            if (employees.isEmpty()) {
                result.put("success", false);
                result.put("error", "No active employees found");
                return ResponseEntity.ok(result);
            }

            // Limit to requested number of employees
            if (numberOfEmployees != null && numberOfEmployees < employees.size()) {
                employees = employees.subList(0, numberOfEmployees);
            }

            // Get orders since June 2025 with English column aliases
            String sqlCommandes = """
                SELECT HEX(o.id) as order_id, 
                       o.num_commande as order_number,
                       o.priority,
                       o.estimated_time_minutes,
                       o.date,
                       COUNT(cco.card_certification_id) as card_count
                FROM `order` o
                LEFT JOIN card_certification_order cco ON o.id = cco.order_id
                WHERE o.status IN (1, 2)
                  AND o.date >= '2025-06-01'
                GROUP BY o.id
                ORDER BY 
                    CASE o.priority 
                        WHEN 'HIGH' THEN 1 
                        WHEN 'MEDIUM' THEN 2 
                        WHEN 'LOW' THEN 3 
                        ELSE 4 
                    END,
                    o.date ASC 
                LIMIT 20
            """;

            Query commandesQuery = entityManager.createNativeQuery(sqlCommandes);
            @SuppressWarnings("unchecked")
            List<Object[]> orders = commandesQuery.getResultList();

            if (orders.isEmpty()) {
                result.put("success", false);
                result.put("error", "No orders found since June 1st, 2025");
                return ResponseEntity.ok(result);
            }

            // Planning algorithm (copied from working French system)
            List<Map<String, Object>> plannings = new ArrayList<>();
            LocalDate planDate = LocalDate.parse(startDate);
            int employeeIndex = 0;
            int savedPlannings = 0;
            int startHour = 9;

            for (Object[] order : orders) {
                String orderId = (String) order[0];
                String orderNumber = (String) order[1];
                String priority = (String) order[2];
                Integer estimatedTime = (Integer) order[3];
                Object originalDate = order[4];
                Long cardCount = ((Number) order[5]).longValue();

                // Select employee (rotation)
                Map<String, Object> employee = employees.get(employeeIndex % employees.size());
                String employeeId = (String) employee.get("id");
                String employeeName = employee.get("prenom") + " " + employee.get("nom");

                // Calculate duration
                int durationMinutes;
                if (estimatedTime != null && estimatedTime > 0) {
                    durationMinutes = estimatedTime;
                } else {
                    durationMinutes = Math.max(60, (int)(cardCount * timePerCard));
                }

                // Adjust by priority
                if ("HIGH".equals(priority)) {
                    durationMinutes = (int)(durationMinutes * 0.8);
                } else if ("LOW".equals(priority)) {
                    durationMinutes = (int)(durationMinutes * 1.2);
                }

                durationMinutes = Math.max(30, Math.min(480, durationMinutes));

                // Calculate times
                LocalDateTime startTime = planDate.atTime(startHour, 0);
                LocalTime startTimeOnly = LocalTime.of(startHour, 0);

                // Check day overflow
                if (startHour + (durationMinutes / 60) >= 17) {
                    planDate = planDate.plusDays(1);
                    startHour = 9;
                    startTime = planDate.atTime(startHour, 0);
                    startTimeOnly = LocalTime.of(startHour, 0);
                }

                // Save to j_planning table (English structure)
                try {
                    String sqlInsert = """
                        INSERT INTO j_planning (
                            id, order_id, employee_id, planning_date, start_time, 
                            duration_minutes, completed, created_at, updated_at
                        ) VALUES (
                            UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, 0, NOW(), NOW()
                        )
                    """;

                    String planningId = UUID.randomUUID().toString().replace("-", "");

                    Query insertQuery = entityManager.createNativeQuery(sqlInsert);
                    insertQuery.setParameter(1, planningId);
                    insertQuery.setParameter(2, orderId.replace("-", ""));
                    insertQuery.setParameter(3, employeeId.replace("-", ""));
                    insertQuery.setParameter(4, planDate);
                    insertQuery.setParameter(5, startTimeOnly);
                    insertQuery.setParameter(6, durationMinutes);

                    int rowsAffected = insertQuery.executeUpdate();
                    if (rowsAffected > 0) {
                        savedPlannings++;
                        System.out.println("✅ " + orderNumber + " → " + employeeName + " on " + planDate);
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error saving " + orderNumber + ": " + e.getMessage());
                }

                // Return data with English field names
                Map<String, Object> planning = new HashMap<>();
                planning.put("orderId", orderId);
                planning.put("orderNumber", orderNumber);
                planning.put("employeeId", employeeId);
                planning.put("employeeName", employeeName);
                planning.put("planningDate", planDate.toString());
                planning.put("startTime", startTime.toString());
                planning.put("durationMinutes", durationMinutes);
                planning.put("priority", priority);
                planning.put("cardCount", cardCount);
                planning.put("originalDate", originalDate);

                plannings.add(planning);

                // Advance to next slot
                employeeIndex++;
                startHour += (durationMinutes / 60) + 1;

                if (startHour >= 16) {
                    planDate = planDate.plusDays(1);
                    startHour = 9;
                    employeeIndex = 0;
                }
            }

            // Force save
            entityManager.flush();

            // English result format
            result.put("success", true);
            result.put("planningsCreated", plannings.size());
            result.put("planningsSaved", savedPlannings);
            result.put("ordersAnalyzed", orders.size());
            result.put("activeEmployees", employees.size());
            result.put("planningStartDate", startDate);
            result.put("ordersPeriod", "Since June 1st, 2025");
            result.put("tableUsed", "j_planning");
            result.put("plannings", plannings);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * View planning - English version using j_planning table
     */
    @GetMapping("/view")
    public ResponseEntity<List<Map<String, Object>>> viewPlanning(
            @RequestParam(required = false) String date) {

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append("    HEX(p.id) as planning_id,");
            sql.append("    HEX(p.order_id) as order_id,");
            sql.append("    HEX(p.employee_id) as employee_id,");
            sql.append("    p.planning_date,");
            sql.append("    p.start_time,");
            sql.append("    p.duration_minutes,");
            sql.append("    p.completed,");
            sql.append("    o.num_commande as order_number,");
            sql.append("    o.priority,");
            sql.append("    CONCAT(e.prenom, ' ', e.nom) as employee_name ");
            sql.append("FROM j_planning p ");
            sql.append("JOIN `order` o ON p.order_id = o.id ");
            sql.append("JOIN j_employee e ON p.employee_id = e.id ");

            if (date != null) {
                sql.append("WHERE p.planning_date = ? ");
            }

            sql.append("ORDER BY p.planning_date, p.start_time");

            Query query = entityManager.createNativeQuery(sql.toString());
            if (date != null) {
                query.setParameter(1, java.sql.Date.valueOf(LocalDate.parse(date)));
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> planning = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> item = new HashMap<>();
                item.put("planningId", row[0]);
                item.put("orderId", row[1]);
                item.put("employeeId", row[2]);
                item.put("planningDate", row[3]);
                item.put("startTime", row[4]);
                item.put("durationMinutes", row[5]);
                item.put("completed", ((Number) row[6]).intValue() == 1);
                item.put("orderNumber", row[7]);
                item.put("priority", row[8]);
                item.put("employeeName", row[9]);
                planning.add(item);
            }

            return ResponseEntity.ok(planning);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Get planning statistics - English version
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Total plannings
            String sqlTotal = "SELECT COUNT(*) FROM j_planning";
            Query totalQuery = entityManager.createNativeQuery(sqlTotal);
            Long totalPlannings = ((Number) totalQuery.getSingleResult()).longValue();

            // By employee
            String sqlByEmployee = """
                SELECT CONCAT(e.prenom, ' ', e.nom) as employee_name, COUNT(*) as planning_count
                FROM j_planning p
                JOIN j_employee e ON p.employee_id = e.id
                GROUP BY p.employee_id, e.prenom, e.nom
                ORDER BY planning_count DESC
            """;

            Query byEmployeeQuery = entityManager.createNativeQuery(sqlByEmployee);
            @SuppressWarnings("unchecked")
            List<Object[]> byEmployeeResults = byEmployeeQuery.getResultList();

            List<Map<String, Object>> employeeStats = new ArrayList<>();
            for (Object[] row : byEmployeeResults) {
                Map<String, Object> emp = new HashMap<>();
                emp.put("employeeName", row[0]);
                emp.put("planningCount", row[1]);
                employeeStats.add(emp);
            }

            stats.put("totalPlannings", totalPlannings);
            stats.put("employeeStats", employeeStats);
            stats.put("status", "SUCCESS");

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            stats.put("error", e.getMessage());
            stats.put("status", "ERROR");
            return ResponseEntity.ok(stats);
        }
    }

    /**
     * Delete old plannings - Cleanup utility
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldPlannings() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Delete plannings older than 30 days and completed
            String sqlDelete = """
                DELETE FROM j_planning 
                WHERE planning_date < CURDATE() - INTERVAL 30 DAY
                  AND completed = 1
            """;

            Query deleteQuery = entityManager.createNativeQuery(sqlDelete);
            int deleted = deleteQuery.executeUpdate();

            result.put("success", true);
            result.put("deletedCount", deleted);
            result.put("message", deleted + " old plannings deleted");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}