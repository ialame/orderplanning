package com.pcagrade.order.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for employee detail operations in the English version
 * Handles individual employee data and their assigned orders/plannings
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeDetailController {

    @Autowired
    private EntityManager entityManager;

    /**
     * Get individual employee details
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<Map<String, Object>> getEmployeeDetails(@PathVariable String employeeId) {
        try {
            System.out.println("👤 Loading employee details for ID: " + employeeId);

            // Clean employee ID (remove dashes if UUID format)
            String cleanEmployeeId = employeeId.replace("-", "");

            String sql = """
                SELECT 
                    HEX(e.id) as id,
                    e.first_name as firstName,
                    e.last_name as lastName,
                    CONCAT(e.first_name, ' ', e.last_name) as fullName,
                    e.email,
                    e.work_hours_per_day as workHoursPerDay,
                    e.available,
                    e.active,
                    e.creation_date as creationDate,
                    e.current_load as currentLoad
                FROM employee e
                WHERE HEX(e.id) = ? AND e.active = true
                """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, cleanEmployeeId);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                System.out.println("❌ Employee not found: " + employeeId);
                return ResponseEntity.notFound().build();
            }

            Object[] row = results.get(0);
            Map<String, Object> employee = new HashMap<>();

            employee.put("id", (String) row[0]);
            employee.put("firstName", (String) row[1]);
            employee.put("lastName", (String) row[2]);
            employee.put("fullName", (String) row[3]);
            employee.put("email", (String) row[4]);
            employee.put("workHoursPerDay", row[5] != null ? ((Number) row[5]).intValue() : 8);
            employee.put("available", row[6] != null ? (Boolean) row[6] : true);
            employee.put("active", row[7] != null ? (Boolean) row[7] : true);
            employee.put("creationDate", row[8]);
            employee.put("currentLoad", row[9] != null ? ((Number) row[9]).intValue() : 0);

            System.out.println("✅ Employee loaded: " + employee.get("fullName"));
            return ResponseEntity.ok(employee);

        } catch (Exception e) {
            System.err.println("❌ Error loading employee details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get plannings/orders assigned to a specific employee
     */
    @GetMapping("/{employeeId}/plannings")
    public ResponseEntity<List<Map<String, Object>>> getEmployeePlannings(
            @PathVariable String employeeId,
            @RequestParam(required = false) String date) {

        try {
            System.out.println("📋 Loading plannings for employee: " + employeeId + " on date: " + date);

            String cleanEmployeeId = employeeId.replace("-", "");

            StringBuilder sqlBuilder = new StringBuilder("""
                SELECT 
                    HEX(jp.id) as id,
                    HEX(jp.order_id) as orderId,
                    jp.order_number as orderNumber,
                    HEX(jp.employee_id) as employeeId,
                    jp.employee_name as employeeName,
                    jp.planning_date as planningDate,
                    jp.start_time as startTime,
                    jp.duration_minutes as durationMinutes,
                    jp.priority,
                    jp.status,
                    jp.card_count as cardCount,
                    jp.notes
                FROM j_planning jp
                WHERE HEX(jp.employee_id) = ?
                """);

            List<Object> parameters = new ArrayList<>();
            parameters.add(cleanEmployeeId);

            if (date != null && !date.isEmpty()) {
                sqlBuilder.append(" AND jp.planning_date = ?");
                parameters.add(date);
            }

            sqlBuilder.append(" ORDER BY jp.planning_date, jp.start_time");

            Query query = entityManager.createNativeQuery(sqlBuilder.toString());
            for (int i = 0; i < parameters.size(); i++) {
                query.setParameter(i + 1, parameters.get(i));
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> plannings = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> planning = new HashMap<>();

                planning.put("id", row[0]);
                planning.put("orderId", row[1]);
                planning.put("orderNumber", row[2]);
                planning.put("employeeId", row[3]);
                planning.put("employeeName", row[4]);
                planning.put("planningDate", row[5]);
                planning.put("startTime", row[6]);
                planning.put("durationMinutes", row[7] != null ? ((Number) row[7]).intValue() : 60);
                planning.put("priority", row[8]);
                planning.put("status", row[9]);
                planning.put("cardCount", row[10] != null ? ((Number) row[10]).intValue() : 1);
                planning.put("notes", row[11]);

                plannings.add(planning);
            }

            System.out.println("✅ Found " + plannings.size() + " plannings for employee");
            return ResponseEntity.ok(plannings);

        } catch (Exception e) {
            System.err.println("❌ Error loading employee plannings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Alternative endpoint using the planning endpoint path
     */
    @GetMapping("/planning/employee/{employeeId}")
    public ResponseEntity<List<Map<String, Object>>> getEmployeePlanningsAlt(
            @PathVariable String employeeId,
            @RequestParam(required = false) String date) {

        return getEmployeePlannings(employeeId, date);
    }

    /**
     * Get cards for a specific order (for the expanded view)
     */
    @GetMapping("/orders/{orderId}/cards")
    public ResponseEntity<Map<String, Object>> getOrderCards(@PathVariable String orderId) {
        try {
            System.out.println("🃏 Loading cards for order: " + orderId);

            String cleanOrderId = orderId.replace("-", "");

            String sql = """
                SELECT 
                    HEX(cc.id) as cardId,
                    cc.code_barre as barcode,
                    COALESCE(cc.type, 'Pokemon') as type,
                    cc.card_id as cardId,
                    COALESCE(cc.annotation, '') as annotation,
                    COALESCE(ct.name, CONCAT('Pokemon Card ', cc.code_barre)) as name,
                    COALESCE(ct.label_name, CONCAT('Card ', cc.code_barre)) as label_name,
                    cc.langue as language,
                    cc.statut_correspondance as status
                FROM card_certification_order cco
                INNER JOIN card_certification cc ON cco.card_certification_id = cc.id
                LEFT JOIN card_translation ct ON cc.card_id = ct.translatable_id 
                WHERE HEX(cco.order_id) = ?
                LIMIT 50
                """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, cleanOrderId);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> cards = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> card = new HashMap<>();
                card.put("id", row[0]);
                card.put("barcode", row[1]);
                card.put("type", row[2]);
                card.put("cardId", row[3]);
                card.put("annotation", row[4]);
                card.put("name", row[5]);
                card.put("label_name", row[6]);
                card.put("language", row[7]);
                card.put("status", row[8]);
                cards.add(card);
            }

            System.out.println("✅ Found " + cards.size() + " cards for order " + orderId);
            return ResponseEntity.ok(Map.of(
                    "orderId", orderId,
                    "cards", cards,
                    "totalCards", cards.size()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error loading order cards: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update planning status (for completing/reopening orders)
     */
    @PutMapping("/planning/{planningId}/status")
    public ResponseEntity<Map<String, Object>> updatePlanningStatus(
            @PathVariable String planningId,
            @RequestBody Map<String, String> request) {

        try {
            String newStatus = request.get("status");
            System.out.println("🔄 Updating planning " + planningId + " to status: " + newStatus);

            String cleanPlanningId = planningId.replace("-", "");

            String sql = """
                UPDATE j_planning 
                SET status = ?, 
                    modification_date = NOW()
                WHERE HEX(id) = ?
                """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, newStatus);
            query.setParameter(2, cleanPlanningId);

            int updated = query.executeUpdate();

            if (updated > 0) {
                System.out.println("✅ Planning status updated successfully");
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Planning status updated to " + newStatus
                ));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.err.println("❌ Error updating planning status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get employee summary statistics
     */
    @GetMapping("/{employeeId}/stats")
    public ResponseEntity<Map<String, Object>> getEmployeeStats(
            @PathVariable String employeeId,
            @RequestParam(required = false) String date) {

        try {
            String cleanEmployeeId = employeeId.replace("-", "");

            StringBuilder sqlBuilder = new StringBuilder("""
                SELECT 
                    COUNT(*) as totalTasks,
                    SUM(jp.card_count) as totalCards,
                    SUM(jp.duration_minutes) as totalMinutes,
                    COUNT(CASE WHEN jp.status = 'COMPLETED' THEN 1 END) as completedTasks,
                    COUNT(CASE WHEN jp.status = 'IN_PROGRESS' THEN 1 END) as inProgressTasks,
                    COUNT(CASE WHEN jp.status = 'SCHEDULED' THEN 1 END) as scheduledTasks
                FROM j_planning jp
                WHERE HEX(jp.employee_id) = ?
                """);

            List<Object> parameters = new ArrayList<>();
            parameters.add(cleanEmployeeId);

            if (date != null && !date.isEmpty()) {
                sqlBuilder.append(" AND jp.planning_date = ?");
                parameters.add(date);
            }

            Query query = entityManager.createNativeQuery(sqlBuilder.toString());
            for (int i = 0; i < parameters.size(); i++) {
                query.setParameter(i + 1, parameters.get(i));
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            if (!results.isEmpty()) {
                Object[] row = results.get(0);
                Map<String, Object> stats = new HashMap<>();

                stats.put("totalTasks", ((Number) row[0]).intValue());
                stats.put("totalCards", row[1] != null ? ((Number) row[1]).intValue() : 0);
                stats.put("totalMinutes", row[2] != null ? ((Number) row[2]).intValue() : 0);
                stats.put("completedTasks", ((Number) row[3]).intValue());
                stats.put("inProgressTasks", ((Number) row[4]).intValue());
                stats.put("scheduledTasks", ((Number) row[5]).intValue());

                return ResponseEntity.ok(stats);
            }

            return ResponseEntity.ok(Map.of(
                    "totalTasks", 0,
                    "totalCards", 0,
                    "totalMinutes", 0,
                    "completedTasks", 0,
                    "inProgressTasks", 0,
                    "scheduledTasks", 0
            ));

        } catch (Exception e) {
            System.err.println("❌ Error loading employee stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}