package com.pcagrade.order.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import com.pcagrade.order.service.EmployeeService;
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

    @Autowired
    private EmployeeService employeService;

    /**
     * Debug endpoint - English version of French diagnostic
     */
    @GetMapping("/debug-real")
    public ResponseEntity<Map<String, Object>> debugReal() {
        Map<String, Object> debug = new HashMap<>();

        try {
            List<Map<String, Object>> activeEmployees = employeService.getTousEmployesActifs();
            debug.put("activeEmployees", activeEmployees.size());

            String sqlDescribe = "DESCRIBE `order`";
            Query describeQuery = entityManager.createNativeQuery(sqlDescribe);
            @SuppressWarnings("unchecked")
            List<Object[]> columns = describeQuery.getResultList();

            List<String> availableColumns = new ArrayList<>();
            for (Object[] col : columns) {
                availableColumns.add((String) col[0]);
            }
            debug.put("availableColumns", availableColumns);

            String sqlCount = "SELECT COUNT(*) FROM `order` WHERE status IN (1, 2) AND date >= '2025-06-01' AND date <= '2025-07-04'";
            Query countQuery = entityManager.createNativeQuery(sqlCount);
            Long availableOrders = ((Number) countQuery.getSingleResult()).longValue();
            debug.put("availableOrders", availableOrders);

            String sqlOrders = "SELECT HEX(o.id), o.num_commande, o.delai, COUNT(cco.card_certification_id) as card_count FROM `order` o LEFT JOIN card_certification_order cco ON o.id = cco.order_id WHERE o.status IN (1, 2) AND o.date >= '2025-06-01' AND o.date <= '2025-07-04' GROUP BY o.id, o.num_commande, o.delai ORDER BY CASE o.delai WHEN 'X' THEN 1 WHEN 'F+' THEN 2 WHEN 'F' THEN 3 WHEN 'E' THEN 4 WHEN 'C' THEN 5 ELSE 6 END, o.date ASC LIMIT 5";

            Query ordersQuery = entityManager.createNativeQuery(sqlOrders);
            @SuppressWarnings("unchecked")
            List<Object[]> sampleOrders = ordersQuery.getResultList();

            List<Map<String, Object>> ordersData = new ArrayList<>();
            for (Object[] row : sampleOrders) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", row[0]);
                order.put("orderNumber", row[1]);
                order.put("delaiCode", row[2]);
                order.put("priority", mapDelaiToPriority((String) row[2]));
                order.put("cardCount", row[3]);
                ordersData.add(order);
            }

            debug.put("sampleOrders", ordersData);
            debug.put("status", "SUCCESS");
            debug.put("message", "English planning system - discovered table structure");

            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            debug.put("error", e.getMessage());
            debug.put("status", "ERROR");
            return ResponseEntity.ok(debug);
        }
    }

    /**
     * Generate planning - COMPLETE IMPLEMENTATION WITH TRANSACTION
     */
    @PostMapping("/generate")
    @Transactional
    public ResponseEntity<Map<String, Object>> generatePlanning(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> result = new HashMap<>();
        long executionStartTime = System.currentTimeMillis();

        try {
            System.out.println("🚀 === ENGLISH PLANNING GENERATION STARTED ===");

            String startDate = (String) request.get("startDate");
            if (startDate == null) startDate = "2025-06-01";

            String endDate = (String) request.get("endDate");
            if (endDate == null) endDate = "2025-07-04";

            Integer numberOfEmployees = (Integer) request.get("numberOfEmployees");
            Integer timePerCard = (Integer) request.get("timePerCard");
            if (timePerCard == null) timePerCard = 3;

            System.out.println("📋 Parameters: startDate=" + startDate +
                    ", endDate=" + endDate +
                    ", timePerCard=" + timePerCard + " min");

            List<Map<String, Object>> employees = employeService.getTousEmployesActifs();
            if (employees.isEmpty()) {
                result.put("success", false);
                result.put("error", "No active employees found");
                return ResponseEntity.ok(result);
            }

            if (numberOfEmployees != null && numberOfEmployees < employees.size()) {
                employees = employees.subList(0, numberOfEmployees);
            }

            System.out.println("👥 Using " + employees.size() + " employees");

            String sqlOrders = "SELECT HEX(o.id) as order_id, o.num_commande as order_number, o.date as order_date, o.delai as priority_code, COUNT(cco.card_certification_id) as actual_card_count FROM `order` o LEFT JOIN card_certification_order cco ON o.id = cco.order_id WHERE o.status IN (1, 2) AND o.date >= ? AND o.date <= ? AND o.date IS NOT NULL GROUP BY o.id, o.num_commande, o.date, o.delai ORDER BY CASE o.delai WHEN 'X' THEN 1 WHEN 'F+' THEN 2 WHEN 'F' THEN 3 WHEN 'E' THEN 4 WHEN 'C' THEN 5 ELSE 6 END, o.date ASC LIMIT 50";

            Query ordersQuery = entityManager.createNativeQuery(sqlOrders);
            ordersQuery.setParameter(1, startDate);
            ordersQuery.setParameter(2, endDate);

            @SuppressWarnings("unchecked")
            List<Object[]> ordersData = ordersQuery.getResultList();

            if (ordersData.isEmpty()) {
                result.put("success", true);
                result.put("message", "No orders to plan in the specified period");
                result.put("ordersAnalyzed", 0);
                result.put("planningsCreated", 0);
                return ResponseEntity.ok(result);
            }

            System.out.println("📦 Found " + ordersData.size() + " orders to plan");

            List<Map<String, Object>> planningsCreated = new ArrayList<>();
            int planningsSaved = 0;
            LocalDate planningStartDate = LocalDate.parse(startDate);
            LocalTime workStartTime = LocalTime.of(9, 0);

            for (int i = 0; i < ordersData.size(); i++) {
                Object[] orderRow = ordersData.get(i);
                String orderId = (String) orderRow[0];
                String orderNumber = (String) orderRow[1];
                String priorityCode = (String) orderRow[3];
                Integer cardCount = ((Number) orderRow[4]).intValue();

                String priority = mapDelaiToPriority(priorityCode);
                int durationMinutes = Math.max(30, cardCount * timePerCard);

                Map<String, Object> employee = employees.get(i % employees.size());
                String employeeId = (String) employee.get("id");
                String employeeName = employee.get("prenom") + " " + employee.get("nom");

                LocalDate planningDate = planningStartDate.plusDays(i / employees.size());
                LocalTime planningStartTime = workStartTime.plusMinutes((i % 8) * 60);

                try {
                    String savePlanningResult = savePlanningToDatabase(
                            orderId, employeeId, planningDate, planningStartTime, durationMinutes, priority, orderNumber
                    );

                    if (savePlanningResult != null) {
                        planningsSaved++;

                        Map<String, Object> planningCreated = new HashMap<>();
                        planningCreated.put("id", savePlanningResult);
                        planningCreated.put("orderId", orderId);
                        planningCreated.put("orderNumber", orderNumber);
                        planningCreated.put("employeeId", employeeId);
                        planningCreated.put("employeeName", employeeName);
                        planningCreated.put("planningDate", planningDate.toString());
                        planningCreated.put("startTime", planningStartTime.toString());
                        planningCreated.put("durationMinutes", durationMinutes);
                        planningCreated.put("priority", priority);
                        planningCreated.put("cardCount", cardCount);

                        planningsCreated.add(planningCreated);

                        System.out.println("✅ " + orderNumber + " → " + employeeName +
                                " (" + planningDate + " " + planningStartTime + ", " + durationMinutes + "min)");
                    }

                } catch (Exception e) {
                    System.err.println("❌ Failed to save planning for " + orderNumber + ": " + e.getMessage());
                }
            }

            long executionTime = System.currentTimeMillis() - executionStartTime;

            result.put("success", true);
            result.put("message", "Planning generation completed successfully");
            result.put("algorithm", "ROUND_ROBIN_ENGLISH");
            result.put("executionTimeMs", executionTime);
            result.put("ordersAnalyzed", ordersData.size());
            result.put("planningsCreated", planningsCreated.size());
            result.put("planningsSaved", planningsSaved);
            result.put("employeesUsed", employees.size());
            result.put("plannings", planningsCreated);
            result.put("period", Map.of("start", startDate, "end", endDate));
            result.put("timestamp", System.currentTimeMillis());

            System.out.println("🎉 PLANNING GENERATION COMPLETED!");
            System.out.println("📊 Result: " + planningsSaved + "/" + ordersData.size() + " orders planned and saved");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Error in planning generation: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", "Planning generation failed: " + e.getMessage());
            result.put("ordersAnalyzed", 0);
            result.put("planningsCreated", 0);
            result.put("planningsSaved", 0);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        }
    }

    /**
     * Save single planning to database with REAL table structure
     */
    private String savePlanningToDatabase(String orderId, String employeeId,
                                          LocalDate planningDate, LocalTime startTime,
                                          int durationMinutes, String priority, String orderNumber) {
        try {
            String planningId = UUID.randomUUID().toString().replace("-", "");

            LocalDateTime startDateTime = planningDate.atTime(startTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);
            LocalDateTime estimatedEndDateTime = endDateTime;

            String sqlInsert = "INSERT INTO j_planning (id, order_id, employee_id, planning_date, start_time, end_time, estimated_duration_minutes, estimated_end_time, priority, status, completed, created_at, updated_at, card_count, notes) VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, 'SCHEDULED', 0, NOW(), NOW(), ?, ?)";

            Query insertQuery = entityManager.createNativeQuery(sqlInsert);
            insertQuery.setParameter(1, planningId);
            insertQuery.setParameter(2, orderId.replace("-", ""));
            insertQuery.setParameter(3, employeeId.replace("-", ""));
            insertQuery.setParameter(4, planningDate);
            insertQuery.setParameter(5, startDateTime);
            insertQuery.setParameter(6, endDateTime);
            insertQuery.setParameter(7, durationMinutes);
            insertQuery.setParameter(8, estimatedEndDateTime);
            insertQuery.setParameter(9, priority);
            insertQuery.setParameter(10, 1);
            insertQuery.setParameter(11, "Generated by English Planning System for order " + orderNumber);

            int rowsAffected = insertQuery.executeUpdate();

            if (rowsAffected > 0) {
                entityManager.flush();
                System.out.println("✅ Planning saved: " + orderNumber + " → " + planningId);
                return planningId;
            } else {
                System.err.println("❌ No rows affected when saving planning for " + orderNumber);
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Database save error for " + orderNumber + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Map delai field to English priority
     */
    private String mapDelaiToPriority(String delai) {
        if (delai == null) return "MEDIUM";

        switch (delai.toUpperCase()) {
            case "X": return "URGENT";
            case "F+": return "HIGH";
            case "F": return "MEDIUM";
            case "E": return "LOW";
            case "C": return "MEDIUM";
            default: return "MEDIUM";
        }
    }

    /**
     * Check planning table structure
     */
    @GetMapping("/check-table")
    public ResponseEntity<Map<String, Object>> checkPlanningTable() {
        Map<String, Object> result = new HashMap<>();

        try {
            String sqlCheckTable = "SHOW TABLES LIKE 'j_planning'";
            Query checkQuery = entityManager.createNativeQuery(sqlCheckTable);
            @SuppressWarnings("unchecked")
            List<String> tables = checkQuery.getResultList();

            boolean tableExists = !tables.isEmpty();
            result.put("tableExists", tableExists);

            if (tableExists) {
                String sqlCount = "SELECT COUNT(*) FROM j_planning";
                Query countQuery = entityManager.createNativeQuery(sqlCount);
                Long planningCount = ((Number) countQuery.getSingleResult()).longValue();
                result.put("existingPlannings", planningCount);
                result.put("status", "TABLE_EXISTS");
                result.put("message", "j_planning table exists with " + planningCount + " records");
            } else {
                result.put("status", "TABLE_MISSING");
                result.put("message", "j_planning table does not exist");
            }

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", "ERROR");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Test single planning save
     */
    @PostMapping("/test-save")
    @Transactional
    public ResponseEntity<Map<String, Object>> testSingleSave() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> employees = employeService.getTousEmployesActifs();
            if (employees.isEmpty()) {
                result.put("success", false);
                result.put("error", "No employees found");
                return ResponseEntity.ok(result);
            }

            Map<String, Object> employee = employees.get(0);
            String employeeId = (String) employee.get("id");
            String employeeName = employee.get("prenom") + " " + employee.get("nom");

            String sqlOrder = "SELECT HEX(o.id) as order_id, o.num_commande FROM `order` o WHERE o.status IN (1, 2) AND o.date >= '2025-06-01' LIMIT 1";

            Query orderQuery = entityManager.createNativeQuery(sqlOrder);
            @SuppressWarnings("unchecked")
            List<Object[]> orders = orderQuery.getResultList();

            if (orders.isEmpty()) {
                result.put("success", false);
                result.put("error", "No orders found");
                return ResponseEntity.ok(result);
            }

            String orderId = (String) orders.get(0)[0];
            String orderNumber = (String) orders.get(0)[1];

            result.put("testData", Map.of(
                    "orderId", orderId,
                    "orderNumber", orderNumber,
                    "employeeId", employeeId,
                    "employeeName", employeeName
            ));

            String planningId = UUID.randomUUID().toString().replace("-", "");
            LocalDate planningDate = LocalDate.now();
            LocalDateTime startDateTime = planningDate.atTime(9, 0);
            LocalDateTime endDateTime = startDateTime.plusMinutes(90);

            String sqlInsert = "INSERT INTO j_planning (id, order_id, employee_id, planning_date, start_time, end_time, estimated_duration_minutes, estimated_end_time, priority, status, completed, created_at, updated_at, card_count, notes) VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)";

            Query insertQuery = entityManager.createNativeQuery(sqlInsert);
            insertQuery.setParameter(1, planningId);
            insertQuery.setParameter(2, orderId.replace("-", ""));
            insertQuery.setParameter(3, employeeId.replace("-", ""));
            insertQuery.setParameter(4, planningDate);
            insertQuery.setParameter(5, startDateTime);
            insertQuery.setParameter(6, endDateTime);
            insertQuery.setParameter(7, 90);
            insertQuery.setParameter(8, endDateTime);
            insertQuery.setParameter(9, "HIGH");
            insertQuery.setParameter(10, "SCHEDULED");
            insertQuery.setParameter(11, 0);
            insertQuery.setParameter(12, 5);
            insertQuery.setParameter(13, "Test planning from API");

            int rowsAffected = insertQuery.executeUpdate();
            entityManager.flush();

            if (rowsAffected > 0) {
                result.put("success", true);
                result.put("message", "Planning saved successfully");
                result.put("planningId", planningId);
                result.put("rowsAffected", rowsAffected);

                String sqlVerify = "SELECT COUNT(*) FROM j_planning WHERE id = UNHEX(?)";
                Query verifyQuery = entityManager.createNativeQuery(sqlVerify);
                verifyQuery.setParameter(1, planningId);
                Long count = ((Number) verifyQuery.getSingleResult()).longValue();
                result.put("verificationCount", count);

            } else {
                result.put("success", false);
                result.put("error", "No rows affected - insert failed");
            }

        } catch (Exception e) {
            System.err.println("❌ Test save error: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());

            if (e.getCause() != null) {
                result.put("rootCause", e.getCause().getMessage());
            }
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Debug planning data in database
     */
    @GetMapping("/debug-plannings")
    public ResponseEntity<Map<String, Object>> debugPlannings() {
        Map<String, Object> debug = new HashMap<>();

        try {
            String sqlCount = "SELECT COUNT(*) FROM j_planning";
            Query countQuery = entityManager.createNativeQuery(sqlCount);
            Long totalPlannings = ((Number) countQuery.getSingleResult()).longValue();
            debug.put("totalPlannings", totalPlannings);

            String sqlRaw = "SELECT HEX(id) as planning_id, HEX(order_id) as order_id, HEX(employee_id) as employee_id, planning_date, start_time, end_time, priority, status, completed FROM j_planning ORDER BY planning_date, start_time LIMIT 5";

            Query rawQuery = entityManager.createNativeQuery(sqlRaw);
            @SuppressWarnings("unchecked")
            List<Object[]> rawResults = rawQuery.getResultList();

            List<Map<String, Object>> rawPlannings = new ArrayList<>();
            for (Object[] row : rawResults) {
                Map<String, Object> planning = new HashMap<>();
                planning.put("id", row[0]);
                planning.put("orderId", row[1]);
                planning.put("employeeId", row[2]);
                planning.put("planningDate", row[3]);
                planning.put("startTime", row[4]);
                planning.put("endTime", row[5]);
                planning.put("priority", row[6]);
                planning.put("status", row[7]);
                planning.put("completed", row[8]);
                rawPlannings.add(planning);
            }
            debug.put("rawPlannings", rawPlannings);
            debug.put("status", "SUCCESS");

        } catch (Exception e) {
            debug.put("error", e.getMessage());
            debug.put("status", "ERROR");
            e.printStackTrace();
        }

        return ResponseEntity.ok(debug);
    }

    /**
     * View planning for specific date - SIMPLIFIED
     */
    @GetMapping("/view-simple")
    public ResponseEntity<List<Map<String, Object>>> viewPlanningSimple(
            @RequestParam(required = false) String date) {

        List<Map<String, Object>> plannings = new ArrayList<>();

        try {
            String sqlView;
            Query viewQuery;

            if (date != null && !date.isEmpty()) {
                sqlView = "SELECT HEX(p.id) as planning_id, HEX(p.order_id) as order_id, HEX(p.employee_id) as employee_id, p.planning_date, p.start_time, p.end_time, p.estimated_duration_minutes, p.priority, p.status, p.completed, p.card_count, p.notes FROM j_planning p WHERE p.planning_date = ? ORDER BY p.start_time ASC";
                viewQuery = entityManager.createNativeQuery(sqlView);
                viewQuery.setParameter(1, date);
            } else {
                sqlView = "SELECT HEX(p.id) as planning_id, HEX(p.order_id) as order_id, HEX(p.employee_id) as employee_id, p.planning_date, p.start_time, p.end_time, p.estimated_duration_minutes, p.priority, p.status, p.completed, p.card_count, p.notes FROM j_planning p ORDER BY p.planning_date, p.start_time ASC LIMIT 10";
                viewQuery = entityManager.createNativeQuery(sqlView);
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = viewQuery.getResultList();

            for (Object[] row : results) {
                Map<String, Object> planning = new HashMap<>();
                planning.put("id", row[0]);
                planning.put("orderId", row[1]);
                planning.put("employeeId", row[2]);
                planning.put("planningDate", row[3]);
                planning.put("startTime", row[4]);
                planning.put("endTime", row[5]);
                planning.put("durationMinutes", row[6]);
                planning.put("priority", row[7]);
                planning.put("status", row[8]);
                planning.put("completed", row[9]);
                planning.put("cardCount", row[10]);
                planning.put("notes", row[11]);

                plannings.add(planning);
            }

            System.out.println("📅 Retrieved " + plannings.size() + " plannings" +
                    (date != null ? " for " + date : ""));

        } catch (Exception e) {
            System.err.println("❌ Error retrieving plannings: " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(plannings);
    }

    /**
     * Get planning statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlanningStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            String sqlTotal = "SELECT COUNT(*) FROM j_planning";
            Query totalQuery = entityManager.createNativeQuery(sqlTotal);
            Long totalPlannings = ((Number) totalQuery.getSingleResult()).longValue();

            String sqlByStatus = "SELECT status, COUNT(*) FROM j_planning GROUP BY status";
            Query statusQuery = entityManager.createNativeQuery(sqlByStatus);
            @SuppressWarnings("unchecked")
            List<Object[]> statusResults = statusQuery.getResultList();

            Map<String, Long> statusCounts = new HashMap<>();
            for (Object[] row : statusResults) {
                statusCounts.put((String) row[0], ((Number) row[1]).longValue());
            }

            stats.put("totalPlannings", totalPlannings);
            stats.put("statusBreakdown", statusCounts);
            stats.put("lastUpdated", System.currentTimeMillis());

        } catch (Exception e) {
            System.err.println("❌ Error getting stats: " + e.getMessage());
            stats.put("error", e.getMessage());
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * View planning for specific date - WITH JOINS
     */
    @GetMapping("/view")
    public ResponseEntity<List<Map<String, Object>>> viewPlanning(
            @RequestParam(required = false) String date) {

        List<Map<String, Object>> plannings = new ArrayList<>();

        try {
            String targetDate = date != null ? date : LocalDate.now().toString();

            String sqlView = "SELECT HEX(p.id) as planning_id, HEX(p.order_id) as order_id, HEX(p.employee_id) as employee_id, p.planning_date, p.start_time, p.end_time, p.estimated_duration_minutes, p.priority, p.status, p.completed, p.card_count, p.notes, CONCAT(e.prenom, ' ', e.nom) as employee_name, o.num_commande as order_number FROM j_planning p LEFT JOIN employe e ON p.employee_id = e.id LEFT JOIN `order` o ON p.order_id = o.id WHERE p.planning_date = ? ORDER BY p.start_time ASC";

            Query viewQuery = entityManager.createNativeQuery(sqlView);
            viewQuery.setParameter(1, targetDate);

            @SuppressWarnings("unchecked")
            List<Object[]> results = viewQuery.getResultList();

            for (Object[] row : results) {
                Map<String, Object> planning = new HashMap<>();
                planning.put("id", row[0]);
                planning.put("orderId", row[1]);
                planning.put("employeeId", row[2]);
                planning.put("planningDate", row[3]);
                planning.put("startTime", row[4]);
                planning.put("endTime", row[5]);
                planning.put("durationMinutes", row[6]);
                planning.put("priority", row[7]);
                planning.put("status", row[8]);
                planning.put("completed", row[9]);
                planning.put("cardCount", row[10]);
                planning.put("notes", row[11]);
                planning.put("employeeName", row[12]);
                planning.put("orderNumber", row[13]);

                plannings.add(planning);
            }

            System.out.println("📅 Retrieved " + plannings.size() + " plannings for " + targetDate);

        } catch (Exception e) {
            System.err.println("❌ Error retrieving plannings: " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(plannings);
    }

    /**
     * Cleanup old plannings
     */
    @DeleteMapping("/cleanup")
    @Transactional
    public ResponseEntity<Map<String, Object>> cleanupPlannings() {
        Map<String, Object> result = new HashMap<>();

        try {
            String sqlDelete = "DELETE FROM j_planning WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY)";
            Query deleteQuery = entityManager.createNativeQuery(sqlDelete);
            int deletedRows = deleteQuery.executeUpdate();

            result.put("success", true);
            result.put("deletedPlannings", deletedRows);
            result.put("message", "Cleanup completed");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}