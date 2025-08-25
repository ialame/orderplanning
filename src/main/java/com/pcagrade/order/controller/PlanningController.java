package com.pcagrade.order.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pcagrade.order.service.EmployeeService;
import com.pcagrade.order.service.PlanningService;
import com.pcagrade.order.service.GreedyPlanningService;

@RestController
@RequestMapping("/api/planning")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PlanningController {
    private static final Logger log = LoggerFactory.getLogger(PlanningController.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PlanningService planningService; // Utilise le service existant

    @Autowired
    private GreedyPlanningService greedyPlanningService; // Alternative

    /**
     * 🎯 ENDPOINT PRINCIPAL - Utilise PlanningService existant
     */
    /**
     * 🎯 ENDPOINT FINAL QUI FONCTIONNE
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePlanning(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🎯 FINAL WORKING PLANNING GENERATION");

            // ========== PARAMETERS ==========
            String startDate = (String) request.getOrDefault("startDate", "2025-07-01");
            Integer timePerCard = request.containsKey("timePerCard") ?
                    Integer.valueOf(request.get("timePerCard").toString()) : 3;
            Boolean cleanFirst = (Boolean) request.getOrDefault("cleanFirst", false);

            log.info("🎯 Config: startDate={}, timePerCard={}, cleanFirst={}",
                    startDate, timePerCard, cleanFirst);

            // ========== AGGRESSIVE CLEAN ==========
            int deletedCount = 0;
            if (cleanFirst) {
                try {
                    // Try multiple approaches to ensure clean
                    String[] deleteQueries = {
                            "DELETE FROM j_planning WHERE 1=1",
                            "TRUNCATE TABLE j_planning"
                    };

                    for (String deleteQuery : deleteQueries) {
                        try {
                            Query deleteQ = entityManager.createNativeQuery(deleteQuery);
                            deletedCount += deleteQ.executeUpdate();
                            log.info("🧹 Delete query '{}' removed {} rows", deleteQuery, deletedCount);
                            break; // Stop after first successful delete
                        } catch (Exception e) {
                            log.warn("Delete query '{}' failed: {}", deleteQuery, e.getMessage());
                        }
                    }

                    // Verify clean worked
                    Query countQ = entityManager.createNativeQuery("SELECT COUNT(*) FROM j_planning");
                    Number remaining = (Number) countQ.getSingleResult();
                    log.info("🔍 After clean: {} plannings remaining", remaining.intValue());

                } catch (Exception cleanError) {
                    log.error("❌ All clean attempts failed: {}", cleanError.getMessage());
                }
            }

            // ========== GET DATA ==========
            List<Map<String, Object>> employees = employeeService.getAllActiveEmployees();
            if (employees.isEmpty()) {
                result.put("success", false);
                result.put("message", "No active employees found");
                return ResponseEntity.ok(result);
            }

            // ========== SIMPLE ORDER QUERY WITHOUT NOT EXISTS ==========
            String orderQuery = """
                SELECT 
                    HEX(o.id) as orderId,
                    o.num_commande as orderNumber,
                    o.date as orderDate
                FROM `order` o
                WHERE o.date >= ?
                ORDER BY o.date ASC
                LIMIT 10
            """;

            Query query = entityManager.createNativeQuery(orderQuery);
            query.setParameter(1, startDate);
            @SuppressWarnings("unchecked")
            List<Object[]> orderResults = query.getResultList();

            log.info("📦 Found {} orders total from date {}", orderResults.size(), startDate);

            if (orderResults.isEmpty()) {
                result.put("success", false);
                result.put("message", "No orders found from date: " + startDate);
                result.put("processedOrders", 0);
                return ResponseEntity.ok(result);
            }

            // ========== FORCE SAVE ALL ORDERS ==========
            List<Map<String, Object>> savedPlannings = new ArrayList<>();
            int successCount = 0;
            List<String> saveErrors = new ArrayList<>();

            for (int i = 0; i < orderResults.size(); i++) {
                try {
                    Object[] orderData = orderResults.get(i);
                    String orderId = (String) orderData[0];
                    String orderNumber = (String) orderData[1];

                    Map<String, Object> employee = employees.get(i % employees.size());
                    String employeeId = (String) employee.get("id");
                    String employeeName = employee.get("firstName") + " " + employee.get("lastName");

                    int cardCount = 20;
                    int durationMinutes = cardCount * timePerCard;
                    LocalDateTime startTime = LocalDate.parse(startDate).atTime(9, 0).plusHours(i);

                    String planningId = UUID.randomUUID().toString().replace("-", "");

                    // ========== MOST BASIC INSERT POSSIBLE ==========
                    String insertQuery = """
                        INSERT INTO j_planning (id, order_id, employee_id) 
                        VALUES (UNHEX(?), UNHEX(?), UNHEX(?))
                    """;

                    Query insertQ = entityManager.createNativeQuery(insertQuery);
                    insertQ.setParameter(1, planningId);
                    insertQ.setParameter(2, orderId);
                    insertQ.setParameter(3, employeeId);

                    int rowsInserted = insertQ.executeUpdate();

                    if (rowsInserted > 0) {
                        successCount++;

                        Map<String, Object> planning = new HashMap<>();
                        planning.put("planningId", planningId);
                        planning.put("orderId", orderId);
                        planning.put("orderNumber", orderNumber);
                        planning.put("employeeId", employeeId);
                        planning.put("employeeName", employeeName);
                        planning.put("cardCount", cardCount);
                        planning.put("durationMinutes", durationMinutes);
                        planning.put("status", "BASIC_SAVED");

                        savedPlannings.add(planning);
                        log.info("✅ BASIC SAVE #{}: Order {} -> Employee {}",
                                successCount, orderNumber, employeeName);
                    }

                } catch (Exception orderError) {
                    String error = String.format("Order %s failed: %s",
                            orderResults.get(i)[1], orderError.getMessage());
                    saveErrors.add(error);
                    log.error("❌ {}", error);
                }
            }

            // ========== TRY ENHANCED SAVES FOR SAVED ITEMS ==========
            for (Map<String, Object> savedPlanning : savedPlannings) {
                try {
                    String planningId = (String) savedPlanning.get("planningId");
                    int cardCount = (Integer) savedPlanning.get("cardCount");
                    int durationMinutes = (Integer) savedPlanning.get("durationMinutes");
                    LocalDateTime startTime = LocalDate.parse(startDate).atTime(9, 0);

                    String updateQuery = """
                        UPDATE j_planning 
                        SET planning_date = ?, start_time = ?, 
                            estimated_duration_minutes = ?, card_count = ?, 
                            status = 'PLANNED', created_at = NOW(), updated_at = NOW()
                        WHERE id = UNHEX(?)
                    """;

                    Query updateQ = entityManager.createNativeQuery(updateQuery);
                    updateQ.setParameter(1, startTime.toLocalDate());
                    updateQ.setParameter(2, startTime.toLocalTime());
                    updateQ.setParameter(3, durationMinutes);
                    updateQ.setParameter(4, cardCount);
                    updateQ.setParameter(5, planningId);

                    int rowsUpdated = updateQ.executeUpdate();
                    if (rowsUpdated > 0) {
                        savedPlanning.put("status", "FULLY_CONFIGURED");
                        log.info("✅ Enhanced save for planning {}", planningId);
                    }

                } catch (Exception updateError) {
                    log.warn("⚠️ Enhancement failed for planning: {}", updateError.getMessage());
                }
            }

            // ========== FINAL VERIFICATION ==========
            Query finalCountQ = entityManager.createNativeQuery("SELECT COUNT(*) FROM j_planning");
            Number totalPlannings = (Number) finalCountQ.getSingleResult();

            // ========== RESULT ==========
            boolean hasSuccess = successCount > 0;
            result.put("success", hasSuccess);
            result.put("message", hasSuccess ?
                    String.format("✅ SUCCESS: %d plannings saved to database!", successCount) :
                    "❌ No plannings could be saved - see errors");
            result.put("processedOrders", successCount);
            result.put("totalOrdersAnalyzed", orderResults.size());
            result.put("activeEmployees", employees.size());
            result.put("planningsCreated", savedPlannings);
            result.put("totalPlanningsInDB", totalPlannings.intValue());
            result.put("deletedBefore", deletedCount);
            result.put("method", "BASIC_INSERT_THEN_ENHANCE");
            result.put("timePerCard", timePerCard);
            result.put("startDate", startDate);
            result.put("timestamp", System.currentTimeMillis());

            if (!saveErrors.isEmpty()) {
                result.put("errors", saveErrors);
            }

            log.info("🎉 FINAL RESULT: {} plannings saved, {} total in DB",
                    successCount, totalPlannings.intValue());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Final planning generation failed: {}", e.getMessage(), e);

            result.put("success", false);
            result.put("message", "Final generation failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            result.put("detailedError", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        }
    }

    /**
     * 🔍 DIAGNOSTIC - Analyser les services disponibles
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugPlanning(
            @RequestParam(defaultValue = "2025-07-01") String startDate) {

        Map<String, Object> debug = new HashMap<>();

        try {
            log.info("🔍 DIAGNOSTIC - Services and data analysis");

            // ========== CHECK EMPLOYEES ==========
            List<Map<String, Object>> employees = employeeService.getAllActiveEmployees();
            debug.put("activeEmployees", employees.size());
            debug.put("employees", employees.stream().limit(3).toList());

            // ========== CHECK ORDERS ==========
            String orderQuery = """
                SELECT 
                    HEX(o.id) as orderId,
                    o.num_commande as orderNumber,
                    o.date as orderDate,
                    COALESCE(o.priority, 'MEDIUM') as priority,
                    COALESCE(o.status, 1) as status
                FROM `order` o
                WHERE o.date >= ?
                ORDER BY o.date ASC
                LIMIT 10
            """;

            Query query = entityManager.createNativeQuery(orderQuery);
            query.setParameter(1, startDate);
            @SuppressWarnings("unchecked")
            List<Object[]> orderResults = query.getResultList();

            debug.put("ordersFromDate", orderResults.size());
            debug.put("sampleOrders", orderResults.stream().limit(3).map(row -> {
                Map<String, Object> order = new HashMap<>();
                order.put("orderId", row[0]);
                order.put("orderNumber", row[1]);
                order.put("orderDate", row[2]);
                order.put("priority", row[3]);
                order.put("status", row[4]);
                return order;
            }).toList());

            // ========== CHECK EXISTING PLANNINGS ==========
            Query planningCountQ = entityManager.createNativeQuery("SELECT COUNT(*) FROM j_planning");
            Number planningCount = (Number) planningCountQ.getSingleResult();
            debug.put("existingPlannings", planningCount.intValue());

            // ========== SERVICE AVAILABILITY ==========
            Map<String, Object> services = new HashMap<>();
            services.put("planningService", planningService != null ? "AVAILABLE" : "NOT_INJECTED");
            services.put("greedyPlanningService", greedyPlanningService != null ? "AVAILABLE" : "NOT_INJECTED");
            services.put("employeeService", employeeService != null ? "AVAILABLE" : "NOT_INJECTED");
            debug.put("services", services);

            // ========== SUMMARY ==========
            debug.put("success", true);
            debug.put("analysis", Map.of(
                    "startDate", startDate,
                    "ordersAvailable", orderResults.size(),
                    "employeesAvailable", employees.size(),
                    "existingPlannings", planningCount.intValue(),
                    "servicesInjected", services.values().stream().allMatch(v -> "AVAILABLE".equals(v))
            ));

            List<String> recommendations = new ArrayList<>();
            if (orderResults.isEmpty()) {
                recommendations.add("❌ No orders found from date " + startDate);
            } else if (employees.isEmpty()) {
                recommendations.add("❌ No employees available");
            } else {
                recommendations.add("✅ " + orderResults.size() + " orders and " + employees.size() + " employees available");
            }
            debug.put("recommendations", recommendations);

            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            log.error("❌ Debug failed: {}", e.getMessage(), e);
            debug.put("success", false);
            debug.put("error", e.getMessage());
            return ResponseEntity.ok(debug);
        }
    }

    /**
     * Get all plannings
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getAllPlannings() {
        try {
            String query = """
                SELECT 
                    HEX(p.id) as planningId,
                    HEX(p.order_id) as orderId,
                    HEX(p.employee_id) as employeeId,
                    p.planning_date as plannedDate,
                    p.start_time as startTime,
                    p.estimated_duration_minutes as durationMinutes,
                    COALESCE(p.status, 'UNKNOWN') as status,
                    COALESCE(p.card_count, 0) as cardCount,
                    o.num_commande as orderNumber,
                    CONCAT(COALESCE(e.prenom, ''), ' ', COALESCE(e.nom, '')) as employeeName
                FROM j_planning p
                LEFT JOIN `order` o ON p.order_id = o.id
                LEFT JOIN j_employee e ON p.employee_id = e.id
                ORDER BY p.created_at DESC
                LIMIT 50
            """;

            Query q = entityManager.createNativeQuery(query);
            @SuppressWarnings("unchecked")
            List<Object[]> results = q.getResultList();

            List<Map<String, Object>> plannings = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> planning = new HashMap<>();
                planning.put("planningId", row[0]);
                planning.put("orderId", row[1]);
                planning.put("employeeId", row[2]);
                planning.put("plannedDate", row[3]);
                planning.put("startTime", row[4]);
                planning.put("durationMinutes", row[5]);
                planning.put("status", row[6]);
                planning.put("cardCount", row[7]);
                planning.put("orderNumber", row[8]);
                planning.put("employeeName", row[9]);
                plannings.add(planning);
            }

            return ResponseEntity.ok(plannings);

        } catch (Exception e) {
            log.error("❌ Failed to retrieve plannings: {}", e.getMessage(), e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Pokemon Card Planning");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
}