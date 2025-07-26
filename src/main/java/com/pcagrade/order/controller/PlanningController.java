// ========== PlanningController.java - VERSION AVEC VRAIES DONNÉES ==========

package com.pcagrade.order.controller;

import com.pcagrade.order.service.EmployeeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Planning Controller - Real Data Version
 * Generates planning using real orders from database
 */
@RestController
@RequestMapping("/api/planning")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class PlanningController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EntityManager entityManager;

    private static final int DEFAULT_MINUTES_PER_CARD = 3;
    private static final int MINIMUM_ORDER_DURATION = 30; // minutes
    private static final int WORK_START_HOUR = 9;
    private static final int WORK_END_HOUR = 17;

    /**
     * 🚀 GENERATE PLANNING WITH REAL DATA
     * Endpoint: POST /api/planning/generate
     */
    @PostMapping("/generate")
    @Transactional
    public ResponseEntity<Map<String, Object>> generatePlanning(@RequestBody Map<String, Object> configData) {
        System.out.println("🚀 === GENERATING PLANNING WITH REAL DATA ===");

        try {
            // Extract configuration
            String dateDebut = extractStringValue(configData, "dateDebut");
            Integer nombreEmployes = extractIntegerValue(configData, "nombreEmployes");
            Integer tempsParCarte = extractIntegerValue(configData, "tempsParCarte");

            // Set defaults
            if (dateDebut == null || dateDebut.trim().isEmpty()) {
                dateDebut = LocalDate.now().toString();
            }
            if (tempsParCarte == null || tempsParCarte <= 0) {
                tempsParCarte = DEFAULT_MINUTES_PER_CARD;
            }

            System.out.println("📋 Planning config: date=" + dateDebut + ", minutesPerCard=" + tempsParCarte);

            // 1. Get active employees
            List<Map<String, Object>> employees = employeeService.getAllActiveEmployees();
            System.out.println("👥 Found " + employees.size() + " active employees");

            if (employees.isEmpty()) {
                return ResponseEntity.ok(createErrorResponse("No active employees found"));
            }

            // 2. Get real orders to plan
            List<Map<String, Object>> orders = getRealOrdersForPlanning(dateDebut);
            System.out.println("📦 Found " + orders.size() + " orders to plan");

            // 3. Generate real planning
            List<Map<String, Object>> planningData = generateRealPlanning(employees, orders, tempsParCarte);

            // 4. Calculate real statistics
            Map<String, Object> stats = calculateRealStats(planningData, orders);

            // 5. Save planning to database (optional)
            savePlanningToDatabase(planningData, dateDebut);

            // 6. Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Planning generated with real data");
            response.put("dateDebut", dateDebut);
            response.put("nombreEmployes", employees.size());
            response.put("tempsParCarte", tempsParCarte);
            response.put("planning", planningData);
            response.put("stats", stats);
            response.put("ordersProcessed", orders.size());
            response.put("timestamp", LocalDateTime.now().toString());

            System.out.println("✅ Real planning generated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error generating real planning: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * 📊 GET REAL ORDERS FOR PLANNING
     */
    private List<Map<String, Object>> getRealOrdersForPlanning(String dateDebut) {
        try {
            System.out.println("📦 Loading real orders since: " + dateDebut);

            // SQL query to get real orders with card counts
            String sql = """
                SELECT 
                    HEX(o.id) as order_id,
                    o.num_commande as order_number,
                    o.date as order_date,
                    o.priorite_string as priority,
                    o.status,
                    COALESCE(o.temps_estime_minutes, 0) as estimated_minutes,
                    COALESCE(o.prix_total, 0) as total_price,
                    COALESCE(card_count.nb_cartes, 0) as card_count
                FROM `order` o
                LEFT JOIN (
                    SELECT 
                        cco.order_id,
                        COUNT(cco.card_certification_id) as nb_cartes
                    FROM card_certification_order cco
                    GROUP BY cco.order_id
                ) card_count ON o.id = card_count.order_id
                WHERE o.date >= ?
                    AND o.status IN (1, 2)
                ORDER BY 
                    CASE o.priorite_string 
                        WHEN 'HAUTE' THEN 1 
                        WHEN 'MOYENNE' THEN 2 
                        ELSE 3 
                    END,
                    o.date ASC
                LIMIT 50
            """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, dateDebut);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> orders = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", (String) row[0]);
                order.put("numeroCommande", (String) row[1]);
                order.put("date", row[2]);
                order.put("priorite", normalizePriority((String) row[3]));
                order.put("status", row[4]);
                order.put("estimatedMinutes", ((Number) row[5]).intValue());
                order.put("totalPrice", row[6] != null ? ((Number) row[6]).doubleValue() : 0.0);
                order.put("cardCount", ((Number) row[7]).intValue());

                orders.add(order);
            }

            System.out.println("✅ Loaded " + orders.size() + " real orders");
            return orders;

        } catch (Exception e) {
            System.err.println("❌ Error loading real orders: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 🎯 GENERATE REAL PLANNING DISTRIBUTION
     */
    private List<Map<String, Object>> generateRealPlanning(
            List<Map<String, Object>> employees,
            List<Map<String, Object>> orders,
            int minutesPerCard) {

        List<Map<String, Object>> planningData = new ArrayList<>();

        // Track employee workload
        Map<String, Integer> employeeWorkload = new HashMap<>();
        Map<String, Integer> employeeOrderCount = new HashMap<>();

        // Initialize employee data
        for (Map<String, Object> employee : employees) {
            String employeeId = (String) employee.get("id");
            employeeWorkload.put(employeeId, 0);
            employeeOrderCount.put(employeeId, 0);

            Map<String, Object> employeeData = new HashMap<>();
            employeeData.put("id", employeeId);
            employeeData.put("nom", employee.get("fullName"));
            employeeData.put("email", employee.get("email"));
            employeeData.put("commandes", new ArrayList<Map<String, Object>>());
            employeeData.put("tempsTotal", 0);
            employeeData.put("totalCartes", 0);

            planningData.add(employeeData);
        }

        // Distribute orders using round-robin with workload balancing
        int employeeIndex = 0;
        LocalTime currentTime = LocalTime.of(WORK_START_HOUR, 0);

        for (Map<String, Object> order : orders) {
            // Find employee with least workload
            String bestEmployeeId = null;
            int minWorkload = Integer.MAX_VALUE;
            int bestEmployeeIndex = 0;

            for (int i = 0; i < employees.size(); i++) {
                String empId = (String) employees.get(i).get("id");
                int workload = employeeWorkload.get(empId);
                if (workload < minWorkload) {
                    minWorkload = workload;
                    bestEmployeeId = empId;
                    bestEmployeeIndex = i;
                }
            }

            if (bestEmployeeId != null) {
                // Calculate order duration
                int cardCount = (Integer) order.get("cardCount");
                int estimatedMinutes = (Integer) order.get("estimatedMinutes");

                int duration = calculateOrderDuration(cardCount, estimatedMinutes, minutesPerCard);

                // Create order entry
                Map<String, Object> orderEntry = new HashMap<>();
                orderEntry.put("id", order.get("id"));
                orderEntry.put("numeroCommande", order.get("numeroCommande"));
                orderEntry.put("priorite", order.get("priorite"));
                orderEntry.put("nombreCartes", cardCount);
                orderEntry.put("dureeEstimee", duration);

                // Calculate time slots
                LocalTime startTime = currentTime.plusMinutes(employeeOrderCount.get(bestEmployeeId) * 15); // 15min buffer
                LocalTime endTime = startTime.plusMinutes(duration);

                // Reset to next day if exceeding work hours
                if (endTime.getHour() >= WORK_END_HOUR) {
                    startTime = LocalTime.of(WORK_START_HOUR, 0);
                    endTime = startTime.plusMinutes(duration);
                }

                orderEntry.put("heureDebut", startTime.toString());
                orderEntry.put("heureFin", endTime.toString());
                orderEntry.put("type", "Real Order");

                // Add to employee's orders
                Map<String, Object> employeeData = planningData.get(bestEmployeeIndex);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> employeeOrders = (List<Map<String, Object>>) employeeData.get("commandes");
                employeeOrders.add(orderEntry);

                // Update employee totals
                int currentTotal = (Integer) employeeData.get("tempsTotal");
                int currentCards = (Integer) employeeData.get("totalCartes");
                employeeData.put("tempsTotal", currentTotal + duration);
                employeeData.put("totalCartes", currentCards + cardCount);

                // Update tracking
                employeeWorkload.put(bestEmployeeId, employeeWorkload.get(bestEmployeeId) + duration);
                employeeOrderCount.put(bestEmployeeId, employeeOrderCount.get(bestEmployeeId) + 1);
            }
        }

        // Calculate efficiency for each employee
        for (Map<String, Object> employeeData : planningData) {
            int totalMinutes = (Integer) employeeData.get("tempsTotal");
            int efficiency = calculateEfficiency(totalMinutes);
            employeeData.put("efficacite", efficiency);
        }

        return planningData;
    }

    /**
     * Calculate order duration based on multiple factors
     */
    private int calculateOrderDuration(int cardCount, int estimatedMinutes, int minutesPerCard) {
        // Priority 1: Use estimated minutes if available and reasonable
        if (estimatedMinutes > 0 && estimatedMinutes <= 8 * 60) { // Max 8 hours
            return estimatedMinutes;
        }

        // Priority 2: Calculate from card count
        if (cardCount > 0) {
            return Math.max(MINIMUM_ORDER_DURATION, cardCount * minutesPerCard);
        }

        // Fallback: default duration
        return 60; // 1 hour default
    }

    /**
     * Save planning to database
     */
    @Transactional
    private void savePlanningToDatabase(List<Map<String, Object>> planningData, String dateDebut) {
        try {
            System.out.println("💾 Saving planning to database...");

            // Clear existing planning for the date
            String sqlDelete = "DELETE FROM j_planning WHERE DATE(date_planification) = ?";
            Query deleteQuery = entityManager.createNativeQuery(sqlDelete);
            deleteQuery.setParameter(1, dateDebut);
            int deletedRows = deleteQuery.executeUpdate();
            System.out.println("🗑️ Deleted " + deletedRows + " existing planning entries");

            // Insert new planning entries
            int savedCount = 0;
            for (Map<String, Object> employee : planningData) {
                String employeeId = (String) employee.get("id");

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> orders = (List<Map<String, Object>>) employee.get("commandes");

                for (Map<String, Object> order : orders) {
                    String sqlInsert = """
                        INSERT INTO j_planning 
                        (id, order_id, employe_id, date_planification, heure_debut, duree_minutes, terminee, date_creation)
                        VALUES (UNHEX(REPLACE(UUID(), '-', '')), UNHEX(?), UNHEX(?), ?, TIME(?), ?, 0, NOW())
                    """;

                    Query insertQuery = entityManager.createNativeQuery(sqlInsert);
                    insertQuery.setParameter(1, ((String) order.get("id")).replace("-", ""));
                    insertQuery.setParameter(2, employeeId.replace("-", ""));
                    insertQuery.setParameter(3, dateDebut);
                    insertQuery.setParameter(4, order.get("heureDebut"));
                    insertQuery.setParameter(5, order.get("dureeEstimee"));

                    insertQuery.executeUpdate();
                    savedCount++;
                }
            }

            System.out.println("✅ Saved " + savedCount + " planning entries to database");

        } catch (Exception e) {
            System.err.println("❌ Error saving planning to database: " + e.getMessage());
            // Don't throw - planning generation can continue without saving
        }
    }

    // ========== UTILITY METHODS ==========

    private String normalizePriority(String priority) {
        if (priority == null) return "MEDIUM";
        return switch (priority.toUpperCase()) {
            case "HAUTE", "HIGH" -> "HIGH";
            case "MOYENNE", "MEDIUM" -> "MEDIUM";
            case "BASSE", "LOW" -> "LOW";
            default -> "MEDIUM";
        };
    }

    private Map<String, Object> calculateRealStats(List<Map<String, Object>> planningData, List<Map<String, Object>> orders) {
        Map<String, Object> stats = new HashMap<>();

        int totalCommandes = orders.size();
        int totalCartes = orders.stream().mapToInt(o -> (Integer) o.get("cardCount")).sum();
        int tempsTotal = planningData.stream()
                .mapToInt(emp -> (Integer) emp.get("tempsTotal"))
                .sum();

        stats.put("totalCommandes", totalCommandes);
        stats.put("totalCartes", totalCartes);
        stats.put("tempsTotal", tempsTotal);
        stats.put("tempsMoyenParCommande", totalCommandes > 0 ? tempsTotal / totalCommandes : 0);
        stats.put("cartesParHeure", tempsTotal > 0 ? (totalCartes * 60) / tempsTotal : 0);
        stats.put("employeesAssigned", planningData.size());

        return stats;
    }

    private int calculateEfficiency(int totalMinutes) {
        int maxDailyMinutes = 8 * 60; // 8 hours = 480 minutes
        return Math.min(100, Math.round((float) totalMinutes / maxDailyMinutes * 100));
    }

    private String extractStringValue(Map<String, Object> config, String key) {
        Object value = config.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer extractIntegerValue(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now().toString());
        return error;
    }

    /**
     * 🔍 DEBUG REAL DATA
     * Endpoint: GET /api/planning/debug-real
     */
    @GetMapping("/debug-real")
    public ResponseEntity<Map<String, Object>> debugRealData() {
        Map<String, Object> debug = new HashMap<>();

        try {
            // Check orders
            String sqlOrders = "SELECT COUNT(*) FROM `order` WHERE status IN (1, 2)";
            Query queryOrders = entityManager.createNativeQuery(sqlOrders);
            Number orderCount = (Number) queryOrders.getSingleResult();
            debug.put("availableOrders", orderCount.intValue());

            // Check employees
            List<Map<String, Object>> employees = employeeService.getAllActiveEmployees();
            debug.put("activeEmployees", employees.size());

            // Sample orders
            String sqlSample = """
                SELECT HEX(o.id), o.num_commande, o.priorite_string, 
                       COALESCE(COUNT(cco.card_certification_id), 0) as card_count
                FROM `order` o
                LEFT JOIN card_certification_order cco ON o.id = cco.order_id
                WHERE o.status IN (1, 2) AND o.date >= CURDATE() - INTERVAL 30 DAY
                GROUP BY o.id
                LIMIT 5
            """;
            Query querySample = entityManager.createNativeQuery(sqlSample);
            @SuppressWarnings("unchecked")
            List<Object[]> sampleData = querySample.getResultList();

            List<Map<String, Object>> sampleOrders = new ArrayList<>();
            for (Object[] row : sampleData) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", row[0]);
                order.put("numero", row[1]);
                order.put("priorite", row[2]);
                order.put("cardCount", ((Number) row[3]).intValue());
                sampleOrders.add(order);
            }
            debug.put("sampleOrders", sampleOrders);

            debug.put("timestamp", LocalDateTime.now().toString());
            debug.put("ready", orderCount.intValue() > 0 && employees.size() > 0);

        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }

        return ResponseEntity.ok(debug);
    }
}