package com.pcagrade.order.controller;

import com.pcagrade.order.entity.Planning;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 🎯 FRONTEND PLANNING CONTROLLER - Version sans conflit
 * Endpoints dédiés au frontend avec préfixe unique
 */
@RestController
@RequestMapping("/api/frontend/planning")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class FrontendPlanningController {

    private static final Logger log = LoggerFactory.getLogger(FrontendPlanningController.class);

    @Autowired
    private EntityManager entityManager;

    /**
     * 📋 GET ALL PLANNINGS - Récupère tous les plannings
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPlannings() {
        try {
            log.info("📋 Fetching all plannings from j_planning table");

            String sql = """
                SELECT 
                    HEX(p.id) as id,
                    HEX(p.order_id) as orderId,
                    HEX(p.employee_id) as employeeId,
                    p.planning_date,
                    p.start_time,
                    p.estimated_duration_minutes,
                    p.priority,
                    p.status,
                    p.completed,
                    p.card_count,
                    p.progress_percentage,
                    p.created_at,
                    p.updated_at,
                    o.num_commande as orderNumber,
                    CONCAT(COALESCE(e.first_name, 'Unknown'), ' ', COALESCE(e.last_name, 'User')) as employeeName
                FROM j_planning p
                LEFT JOIN `order` o ON p.order_id = o.id  
                LEFT JOIN j_employee e ON p.employee_id = e.id
                ORDER BY p.planning_date ASC, p.start_time ASC
                """;

            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> plannings = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> planning = new HashMap<>();
                planning.put("id", row[0]);
                planning.put("orderId", row[1]);
                planning.put("employeeId", row[2]);
                planning.put("planningDate", row[3]);
                planning.put("startTime", row[4]);
                planning.put("estimatedDurationMinutes", row[5]);
                planning.put("priority", row[6]);
                planning.put("status", row[7]);
                planning.put("completed", row[8]);
                planning.put("cardCount", row[9]);
                planning.put("progressPercentage", row[10]);
                planning.put("createdAt", row[11]);
                planning.put("updatedAt", row[12]);
                planning.put("orderNumber", row[13]);
                planning.put("employeeName", row[14]);

                // Formatted duration
                Integer duration = (Integer) row[5];
                if (duration != null) {
                    planning.put("formattedDuration", formatDuration(duration));
                    planning.put("estimatedHours", Math.round(duration / 60.0 * 100.0) / 100.0);
                }

                plannings.add(planning);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("plannings", plannings);
            response.put("total", plannings.size());

            log.info("✅ Retrieved {} plannings successfully", plannings.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error fetching plannings", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 👥 GET EMPLOYEE PLANNINGS - Plannings d'un employé spécifique
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Map<String, Object>> getEmployeePlannings(
            @PathVariable String employeeId,
            @RequestParam(required = false) String date) {

        try {
            log.info("👥 Fetching plannings for employee: {}, date: {}", employeeId, date);

            String sql = """
                SELECT 
                    HEX(p.id) as id,
                    HEX(p.order_id) as orderId,
                    p.planning_date,
                    p.start_time,
                    p.estimated_duration_minutes,
                    p.priority,
                    p.status,
                    p.completed,
                    p.card_count,
                    p.progress_percentage,
                    o.num_commande as orderNumber,
                    CONCAT(COALESCE(e.first_name, 'Unknown'), ' ', COALESCE(e.last_name, 'User')) as employeeName
                FROM j_planning p
                LEFT JOIN `order` o ON p.order_id = o.id  
                LEFT JOIN j_employee e ON p.employee_id = e.id
                WHERE HEX(p.employee_id) = ?
                """ + (date != null ? " AND p.planning_date = ?" : "") + """
                ORDER BY p.planning_date ASC, p.start_time ASC
                """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, employeeId.toUpperCase());
            if (date != null) {
                query.setParameter(2, LocalDate.parse(date));
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> plannings = new ArrayList<>();
            int totalDuration = 0;
            int totalCards = 0;

            for (Object[] row : results) {
                Map<String, Object> planning = new HashMap<>();
                planning.put("id", row[0]);
                planning.put("orderId", row[1]);
                planning.put("planningDate", row[2]);
                planning.put("startTime", row[3]);
                planning.put("estimatedDurationMinutes", row[4]);
                planning.put("priority", row[5]);
                planning.put("status", row[6]);
                planning.put("completed", row[7]);
                planning.put("cardCount", row[8]);
                planning.put("progressPercentage", row[9]);
                planning.put("orderNumber", row[10]);
                planning.put("employeeName", row[11]);

                Integer duration = (Integer) row[4];
                Integer cardCount = (Integer) row[8];

                if (duration != null) {
                    planning.put("formattedDuration", formatDuration(duration));
                    totalDuration += duration;
                }

                if (cardCount != null) {
                    totalCards += cardCount;
                }

                plannings.add(planning);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("plannings", plannings);
            response.put("total", plannings.size());
            response.put("totalDurationMinutes", totalDuration);
            response.put("totalHours", Math.round(totalDuration / 60.0 * 100.0) / 100.0);
            response.put("totalCards", totalCards);

            log.info("✅ Retrieved {} plannings for employee {}", plannings.size(), employeeId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error fetching employee plannings", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 📊 GET EMPLOYEES WITH PLANNING DATA - Employés avec leurs statistiques
     */
    @GetMapping("/employees-stats")
    public ResponseEntity<Map<String, Object>> getEmployeesWithPlanningStats(
            @RequestParam(required = false) String date) {

        try {
            log.info("📊 Fetching employees with planning stats for date: {}", date);

            String dateFilter = date != null ? " AND p.planning_date = '" + date + "'" : "";

            String sql = """
                SELECT 
                    HEX(e.id) as employeeId,
                    CONCAT(COALESCE(e.first_name, 'Unknown'), ' ', COALESCE(e.last_name, 'User')) as name,
                    e.first_name,
                    e.last_name,
                    e.email,
                    e.active,
                    COALESCE(e.work_hours_per_day, 8) as workHoursPerDay,
                    COALESCE(SUM(p.estimated_duration_minutes), 0) as totalMinutes,
                    COUNT(p.id) as taskCount,
                    COALESCE(SUM(p.card_count), 0) as cardCount,
                    ROUND(COALESCE(SUM(p.estimated_duration_minutes), 0) / (COALESCE(e.work_hours_per_day, 8) * 60.0), 2) as workloadRatio
                FROM j_employee e
                LEFT JOIN j_planning p ON e.id = p.employee_id""" + dateFilter + """
                GROUP BY e.id, e.first_name, e.last_name, e.email, e.active, e.work_hours_per_day
                ORDER BY workloadRatio DESC, name ASC
                """;

            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> employees = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> employee = new HashMap<>();
                employee.put("id", row[0]);
                employee.put("name", row[1]);
                employee.put("firstName", row[2]);
                employee.put("lastName", row[3]);
                employee.put("email", row[4]);
                employee.put("active", row[5]);
                employee.put("workHoursPerDay", row[6]);
                employee.put("totalMinutes", row[7]);
                employee.put("maxMinutes", ((Number) row[6]).intValue() * 60);
                employee.put("taskCount", row[8]);
                employee.put("cardCount", row[9]);
                employee.put("workloadRatio", row[10]);

                // Status based on workload
                Double workloadRatio = (Double) row[10];
                String status;
                if (workloadRatio >= 1.0) {
                    status = "overloaded";
                } else if (workloadRatio >= 0.8) {
                    status = "busy";
                } else {
                    status = "available";
                }
                employee.put("status", status);
                employee.put("available", workloadRatio < 0.8);

                employees.add(employee);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("employees", employees);
            response.put("total", employees.size());
            response.put("date", date != null ? date : "all");

            log.info("✅ Retrieved {} employees with planning stats", employees.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error fetching employees with planning stats", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 🗑️ DELETE ALL PLANNINGS - Nettoie la table j_planning
     */
    @DeleteMapping("/cleanup")
    @Transactional
    public ResponseEntity<Map<String, Object>> cleanupPlannings() {
        try {
            log.info("🗑️ Cleaning up j_planning table");

            String countSql = "SELECT COUNT(*) FROM j_planning";
            Query countQuery = entityManager.createNativeQuery(countSql);
            Number beforeCount = (Number) countQuery.getSingleResult();

            String deleteSql = "DELETE FROM j_planning";
            Query deleteQuery = entityManager.createNativeQuery(deleteSql);
            int deletedRows = deleteQuery.executeUpdate();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Planning table cleaned successfully");
            response.put("rowsDeleted", deletedRows);
            response.put("beforeCount", beforeCount.intValue());

            log.info("✅ Deleted {} planning records", deletedRows);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error cleaning planning table", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 📈 GET PLANNING STATS - Statistiques globales
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlanningStats() {
        try {
            log.info("📈 Fetching planning statistics");

            String sql = """
                SELECT 
                    COUNT(*) as totalPlannings,
                    COUNT(DISTINCT employee_id) as employeesUsed,
                    COUNT(DISTINCT order_id) as ordersPlanned,
                    SUM(card_count) as totalCards,
                    SUM(estimated_duration_minutes) as totalMinutes,
                    AVG(estimated_duration_minutes) as avgDuration,
                    COUNT(CASE WHEN status = 'SCHEDULED' THEN 1 END) as scheduled,
                    COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as inProgress,
                    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
                    COUNT(CASE WHEN priority = 'HIGH' THEN 1 END) as highPriority,
                    MIN(planning_date) as earliestDate,
                    MAX(planning_date) as latestDate
                FROM j_planning
                """;

            Query query = entityManager.createNativeQuery(sql);
            Object[] result = (Object[]) query.getSingleResult();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPlannings", result[0]);
            stats.put("employeesUsed", result[1]);
            stats.put("ordersPlanned", result[2]);
            stats.put("totalCards", result[3]);
            stats.put("totalMinutes", result[4]);
            stats.put("totalHours", result[4] != null ? Math.round(((Number) result[4]).doubleValue() / 60.0 * 100.0) / 100.0 : 0);
            stats.put("avgDuration", result[5] != null ? Math.round(((Number) result[5]).doubleValue() * 100.0) / 100.0 : 0);
            stats.put("scheduled", result[6]);
            stats.put("inProgress", result[7]);
            stats.put("completed", result[8]);
            stats.put("highPriority", result[9]);
            stats.put("earliestDate", result[10]);
            stats.put("latestDate", result[11]);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);

            log.info("✅ Planning stats retrieved successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error fetching planning stats", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ========== UTILITY METHODS ==========

    private String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + "min";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        return remainingMinutes > 0 ? hours + "h" + remainingMinutes + "min" : hours + "h";
    }
}