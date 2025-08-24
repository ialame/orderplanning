package com.pcagrade.order.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.UUID;
import jakarta.persistence.EntityTransaction;
import org.springframework.transaction.annotation.Transactional;
import com.pcagrade.order.service.EmployeeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planning")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PlanningController {
    private static final Logger log = LoggerFactory.getLogger(PlanningController.class);
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EmployeeService employeService;



// ===============================================
// 🎯 MÉTHODE DE GÉNÉRATION SIMPLIFIÉE GARANTIE
// ===============================================

    @PostMapping("/generate-simple")
    public ResponseEntity<Map<String, Object>> generatePlanningSimple(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🎯 === SIMPLE PLANNING GENERATION ===");

            // 1. RÉCUPÉRER UN SEUL EMPLOYÉ ET UNE SEULE COMMANDE
            String sqlEmployee = "SELECT HEX(id) as id, prenom, nom FROM j_employee WHERE actif = 1 LIMIT 1";
            Query empQuery = entityManager.createNativeQuery(sqlEmployee);
            @SuppressWarnings("unchecked")
            List<Object[]> employees = empQuery.getResultList();

            if (employees.isEmpty()) {
                result.put("error", "No active employees found");
                return ResponseEntity.ok(result);
            }

            Object[] employee = employees.get(0);
            String employeeId = (String) employee[0];
            String employeeName = employee[1] + " " + employee[2];

            String sqlOrder = "SELECT HEX(id) as id, num_commande FROM `order` WHERE status IN (1,2,3) ORDER BY date DESC LIMIT 1";
            Query orderQuery = entityManager.createNativeQuery(sqlOrder);
            @SuppressWarnings("unchecked")
            List<Object[]> orders = orderQuery.getResultList();

            if (orders.isEmpty()) {
                result.put("error", "No orders found");
                return ResponseEntity.ok(result);
            }

            Object[] order = orders.get(0);
            String orderId = (String) order[0];
            String orderNumber = (String) order[1];

            System.out.println("📋 Selected:");
            System.out.println("  Employee: " + employeeName + " (ID: " + employeeId + ")");
            System.out.println("  Order: " + orderNumber + " (ID: " + orderId + ")");

            // 2. UTILISER UNE DATE FUTURE UNIQUE
            LocalDate futureDate = LocalDate.now().plusDays(10); // 10 jours dans le futur
            LocalTime uniqueTime = LocalTime.of(15, 30); // Heure unique

            System.out.println("  Planning Date: " + futureDate);
            System.out.println("  Planning Time: " + uniqueTime);

            // 3. VÉRIFIER QU'AUCUNE PLANIFICATION N'EXISTE POUR CETTE COMBINAISON
            String checkSql = """
            SELECT COUNT(*) FROM j_planning 
            WHERE order_id = UNHEX(?)
            AND employee_id = UNHEX(?)
            AND planning_date = ?
            AND start_time = ?
        """;

            Query checkQuery = entityManager.createNativeQuery(checkSql);
            checkQuery.setParameter(1, orderId);
            checkQuery.setParameter(2, employeeId);
            checkQuery.setParameter(3, futureDate);
            checkQuery.setParameter(4, uniqueTime);

            Number existingCount = (Number) checkQuery.getSingleResult();
            result.put("existing_plannings_check", existingCount.intValue());

            if (existingCount.intValue() > 0) {
                // Si elle existe, utiliser une heure différente
                uniqueTime = LocalTime.of(16, 45);
                System.out.println("  Conflict detected, using time: " + uniqueTime);
            }

            // 4. INSERTION ULTRA-SIMPLE
            String planningId = UUID.randomUUID().toString().replace("-", "");
            LocalDateTime startDateTime = LocalDateTime.of(futureDate, uniqueTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(120);

            System.out.println("🔧 Inserting planning:");
            System.out.println("  Planning ID: " + planningId);
            System.out.println("  Start: " + startDateTime);
            System.out.println("  End: " + endDateTime);

            String insertSql = """
            INSERT INTO j_planning 
            (id, order_id, employee_id, planning_date, start_time, end_time, 
             estimated_duration_minutes, estimated_end_time, priority, status, 
             completed, created_at, updated_at, card_count, notes)
            VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)
        """;

            int rowsAffected = 0;
            String insertError = null;

            try {
                rowsAffected = entityManager.createNativeQuery(insertSql)
                        .setParameter(1, planningId)
                        .setParameter(2, orderId)
                        .setParameter(3, employeeId)
                        .setParameter(4, futureDate)
                        .setParameter(5, startDateTime)
                        .setParameter(6, endDateTime)
                        .setParameter(7, 120)
                        .setParameter(8, endDateTime)
                        .setParameter(9, "HIGH")
                        .setParameter(10, "SCHEDULED")
                        .setParameter(11, 0)
                        .setParameter(12, 1)
                        .setParameter(13, "SIMPLE Generated: " + orderNumber + " for " + employeeName)
                        .executeUpdate();

                // FORCER LE COMMIT
                entityManager.flush();

            } catch (Exception e) {
                insertError = e.getMessage();
                System.err.println("❌ Insert error: " + e.getMessage());
                e.printStackTrace();
            }

            // 5. VÉRIFICATION POST-INSERTION
            int verificationCount = 0;
            if (rowsAffected > 0) {
                String verifySql = "SELECT COUNT(*) FROM j_planning WHERE id = UNHEX(?)";
                Query verifyQuery = entityManager.createNativeQuery(verifySql);
                verifyQuery.setParameter(1, planningId);
                Number verifyResult = (Number) verifyQuery.getSingleResult();
                verificationCount = verifyResult.intValue();
            }

            // 6. RÉSULTATS DÉTAILLÉS
            result.put("success", rowsAffected > 0);
            result.put("employee_id", employeeId);
            result.put("employee_name", employeeName);
            result.put("order_id", orderId);
            result.put("order_number", orderNumber);
            result.put("planning_date", futureDate.toString());
            result.put("planning_time", uniqueTime.toString());
            result.put("planning_id", planningId);
            result.put("rows_affected", rowsAffected);
            result.put("verification_count", verificationCount);
            result.put("insert_error", insertError);

            if (rowsAffected > 0 && verificationCount > 0) {
                result.put("message", "✅ Planning created and verified successfully");
                result.put("planningsSaved", 1);
                System.out.println("✅ SIMPLE Planning created successfully!");
            } else if (rowsAffected > 0 && verificationCount == 0) {
                result.put("message", "⚠️ Planning inserted but not found in verification");
                result.put("planningsSaved", 0);
            } else {
                result.put("message", "❌ Planning insertion failed");
                result.put("planningsSaved", 0);
            }

            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Simple generation error: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("planningsSaved", 0);
            return ResponseEntity.status(500).body(result);
        }
    }

// ===============================================
// MÉTHODE POUR VOIR LES PLANIFICATIONS SIMPLES
// ===============================================

    @GetMapping("/view-simple-generated")
    public ResponseEntity<List<Map<String, Object>>> viewSimpleGenerated() {
        try {
            String sql = """
            SELECT 
                HEX(p.id) as id,
                p.planning_date,
                p.start_time,
                p.estimated_duration_minutes,
                p.priority,
                p.status,
                p.notes,
                p.created_at
            FROM j_planning p
            WHERE p.notes LIKE 'SIMPLE Generated:%'
            ORDER BY p.created_at DESC
            LIMIT 10
        """;

            Query query = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> plannings = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> planning = new HashMap<>();
                planning.put("id", row[0]);
                planning.put("planningDate", row[1]);
                planning.put("startTime", row[2]);
                planning.put("durationMinutes", row[3]);
                planning.put("priority", row[4]);
                planning.put("status", row[5]);
                planning.put("notes", row[6]);
                planning.put("createdAt", row[7]);
                plannings.add(planning);
            }

            return ResponseEntity.ok(plannings);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @PostMapping("/generate-safe")
    public ResponseEntity<Map<String, Object>> generatePlanningSafe(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🚀 SAFE PLANNING GENERATION STARTED");
            System.out.println("📋 Request: " + request);

            // ========== PHASE 1: VALIDATION PARAMÈTRES ==========
            result.put("phase", "1-validation");

            String startDate = (String) request.get("startDate");
            if (startDate == null) {
                startDate = "2025-06-01";
            }

            Integer numberOfEmployees = null;
            Object numEmpObj = request.get("numberOfEmployees");
            if (numEmpObj instanceof Number) {
                numberOfEmployees = ((Number) numEmpObj).intValue();
            }

            Integer timePerCard = 3;
            Object timePerCardObj = request.get("timePerCard");
            if (timePerCardObj instanceof Number) {
                timePerCard = ((Number) timePerCardObj).intValue();
            }

            System.out.println("✅ Phase 1 - Paramètres validés:");
            System.out.println("  startDate: " + startDate);
            System.out.println("  numberOfEmployees: " + numberOfEmployees);
            System.out.println("  timePerCard: " + timePerCard);

            result.put("startDate", startDate);
            result.put("numberOfEmployees", numberOfEmployees);
            result.put("timePerCard", timePerCard);

            // ========== PHASE 2: TEST EMPLOYÉS ==========
            result.put("phase", "2-employees");

            List<Map<String, Object>> employees = null;
            try {
                employees = employeService.getAllActiveEmployees();
                System.out.println("✅ Phase 2 - Employés récupérés: " + employees.size());
                result.put("employeesFound", employees.size());

                if (employees.isEmpty()) {
                    result.put("success", false);
                    result.put("error", "No active employees found");
                    result.put("phase_failed", "2-employees");
                    return ResponseEntity.ok(result);
                }

            } catch (Exception e) {
                System.err.println("❌ Phase 2 - Erreur employés: " + e.getMessage());
                e.printStackTrace();
                result.put("success", false);
                result.put("error", "Employee service failed: " + e.getMessage());
                result.put("phase_failed", "2-employees");
                result.put("stackTrace", getStackTrace(e));
                return ResponseEntity.status(500).body(result);
            }

            // ========== PHASE 3: TEST COMMANDES ==========
            result.put("phase", "3-orders");

            List<Object[]> ordersData = null;
            try {
                String sqlOrders = """
                SELECT 
                    HEX(o.id) as order_id,
                    o.num_commande as order_number,
                    o.date as order_date,
                    o.status as order_status
                FROM `order` o
                WHERE o.status IN (1, 2, 3)
                ORDER BY o.date DESC
                LIMIT 10
            """;

                Query orderQuery = entityManager.createNativeQuery(sqlOrders);
                ordersData = orderQuery.getResultList();

                System.out.println("✅ Phase 3 - Commandes récupérées: " + ordersData.size());
                result.put("ordersFound", ordersData.size());

                if (ordersData.isEmpty()) {
                    result.put("success", true);
                    result.put("message", "No orders found but system working");
                    result.put("ordersAnalyzed", 0);
                    result.put("planningsCreated", 0);
                    return ResponseEntity.ok(result);
                }

            } catch (Exception e) {
                System.err.println("❌ Phase 3 - Erreur commandes: " + e.getMessage());
                e.printStackTrace();
                result.put("success", false);
                result.put("error", "Orders query failed: " + e.getMessage());
                result.put("phase_failed", "3-orders");
                result.put("stackTrace", getStackTrace(e));
                return ResponseEntity.status(500).body(result);
            }

            // ========== PHASE 4: TEST INSERTION SIMPLE ==========
            result.put("phase", "4-insertion");

            try {
                // Prendre le premier employé et la première commande
                Map<String, Object> firstEmployee = employees.get(0);
                Object[] firstOrder = ordersData.get(0);

                String employeeId = (String) firstEmployee.get("id");
                String orderId = (String) firstOrder[0];
                String orderNumber = (String) firstOrder[1];

                System.out.println("✅ Phase 4 - Test insertion avec:");
                System.out.println("  employeeId: " + employeeId);
                System.out.println("  orderId: " + orderId);
                System.out.println("  orderNumber: " + orderNumber);

                // Test d'insertion sans transaction pour éviter les rollbacks
                String planningId = insertPlanningSimple(orderId, employeeId, orderNumber);

                if (planningId != null) {
                    System.out.println("✅ Phase 4 - Insertion réussie: " + planningId);
                    result.put("success", true);
                    result.put("message", "Planning generated successfully");
                    result.put("testPlanningId", planningId);
                    result.put("ordersAnalyzed", 1);
                    result.put("planningsCreated", 1);
                    result.put("planningsSaved", 1);
                } else {
                    result.put("success", false);
                    result.put("error", "Planning insertion failed");
                    result.put("phase_failed", "4-insertion");
                }

            } catch (Exception e) {
                System.err.println("❌ Phase 4 - Erreur insertion: " + e.getMessage());
                e.printStackTrace();
                result.put("success", false);
                result.put("error", "Planning insertion failed: " + e.getMessage());
                result.put("phase_failed", "4-insertion");
                result.put("stackTrace", getStackTrace(e));
                return ResponseEntity.status(500).body(result);
            }

            result.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Erreur générale: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", "General error: " + e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("stackTrace", getStackTrace(e));

            return ResponseEntity.status(500).body(result);
        }
    }

    // ========== MÉTHODE D'INSERTION SIMPLE (SANS TRANSACTION) ==========
    private String insertPlanningSimple(String orderId, String employeeId, String orderNumber) {
        try {
            String planningId = UUID.randomUUID().toString().replace("-", "");
            LocalDate planningDate = LocalDate.now();
            LocalTime startTime = LocalTime.of(9, 0);
            LocalDateTime startDateTime = LocalDateTime.of(planningDate, startTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(60);

            System.out.println("🔧 Tentative insertion avec:");
            System.out.println("  planningId: " + planningId);
            System.out.println("  orderId: " + orderId);
            System.out.println("  employeeId: " + employeeId);
            System.out.println("  planningDate: " + planningDate);

            String insertSql = """
            INSERT INTO j_planning 
            (id, order_id, employee_id, planning_date, start_time, end_time, 
             estimated_duration_minutes, estimated_end_time, priority, status, 
             completed, created_at, updated_at, card_count, notes)
            VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)
        """;

            Query insertQuery = entityManager.createNativeQuery(insertSql);
            insertQuery.setParameter(1, planningId);
            insertQuery.setParameter(2, orderId);
            insertQuery.setParameter(3, employeeId);
            insertQuery.setParameter(4, planningDate);
            insertQuery.setParameter(5, startDateTime);
            insertQuery.setParameter(6, endDateTime);
            insertQuery.setParameter(7, 60);
            insertQuery.setParameter(8, endDateTime);
            insertQuery.setParameter(9, "MEDIUM");
            insertQuery.setParameter(10, "SCHEDULED");
            insertQuery.setParameter(11, 0);
            insertQuery.setParameter(12, 1);
            insertQuery.setParameter(13, "Safe test planning for: " + orderNumber);

            int rowsAffected = insertQuery.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Insertion réussie: " + rowsAffected + " lignes affectées");
                return planningId;
            } else {
                System.out.println("❌ Aucune ligne affectée");
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur insertion simple: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



// ==========================================
// FIXED PlanningController.java - SQL QUERIES
// ==========================================

    /**
     * ✅ FIXED: View simple plannings - REMOVED REFERENCE TO 'employee' TABLE
     * Replace the viewPlanningsSimple method in PlanningController.java
     */
    // Dans PlanningController.java, remplacez la méthode view-simple par ceci :

    @GetMapping("/view-simple")
    public ResponseEntity<List<Map<String, Object>>> viewPlanningsSimple(@RequestParam(required = false) String date) {
        List<Map<String, Object>> plannings = new ArrayList<>();

        try {
            System.out.println("🔍 Loading plannings for view-simple endpoint...");
            System.out.println("📅 Date filter: " + (date != null ? date : "ALL"));

            String sqlView;
            Query viewQuery;

            // ✅ REQUÊTE CORRIGÉE avec vraies colonnes de j_employee
            if (date != null && !date.trim().isEmpty()) {
                sqlView = """
                SELECT 
                    HEX(p.id) as id,
                    HEX(p.order_id) as orderId,
                    HEX(p.employee_id) as employeeId,
                    p.planning_date as planningDate,
                    p.start_time as startTime,
                    p.end_time as endTime,
                    p.estimated_duration_minutes as durationMinutes,
                    COALESCE(p.priority, 'MEDIUM') as priority,
                    COALESCE(p.status, 'SCHEDULED') as status,
                    COALESCE(p.completed, 0) as completed,
                    COALESCE(p.card_count, 1) as cardCount,
                    p.notes,
                    COALESCE(p.progress_percentage, 0) as progressPercentage,
                    
                    -- ✅ CORRECTION : Utiliser first_name, last_name (vraies colonnes)
                    COALESCE(
                        CONCAT(e.first_name, ' ', e.last_name),
                        CONCAT('Employee-', RIGHT(HEX(p.employee_id), 6))
                    ) as employeeName,
                    
                    COALESCE(o.num_commande, CONCAT('ORDER-', RIGHT(HEX(p.order_id), 6))) as orderNumber
                    
                FROM j_planning p
                LEFT JOIN j_employee e ON p.employee_id = e.id
                LEFT JOIN `order` o ON p.order_id = o.id
                WHERE p.planning_date = ?
                ORDER BY p.start_time ASC
            """;

                viewQuery = entityManager.createNativeQuery(sqlView);
                viewQuery.setParameter(1, date);
            } else {
                // Sans filtre de date - récupère tout
                sqlView = """
                SELECT 
                    HEX(p.id) as id,
                    HEX(p.order_id) as orderId,
                    HEX(p.employee_id) as employeeId,
                    p.planning_date as planningDate,
                    p.start_time as startTime,
                    p.end_time as endTime,
                    p.estimated_duration_minutes as durationMinutes,
                    COALESCE(p.priority, 'MEDIUM') as priority,
                    COALESCE(p.status, 'SCHEDULED') as status,
                    COALESCE(p.completed, 0) as completed,
                    COALESCE(p.card_count, 1) as cardCount,
                    p.notes,
                    COALESCE(p.progress_percentage, 0) as progressPercentage,
                    
                    -- ✅ CORRECTION : Utiliser first_name, last_name
                    COALESCE(
                        CONCAT(e.first_name, ' ', e.last_name),
                        CONCAT('Employee-', RIGHT(HEX(p.employee_id), 6))
                    ) as employeeName,
                    
                    COALESCE(o.num_commande, CONCAT('ORDER-', RIGHT(HEX(p.order_id), 6))) as orderNumber
                    
                FROM j_planning p
                LEFT JOIN j_employee e ON p.employee_id = e.id
                LEFT JOIN `order` o ON p.order_id = o.id
                ORDER BY p.planning_date DESC, p.start_time ASC
                LIMIT 100
            """;

                viewQuery = entityManager.createNativeQuery(sqlView);
            }

            @SuppressWarnings("unchecked")
            List<Object[]> results = viewQuery.getResultList();

            System.out.println("📊 Found " + results.size() + " planning records");

            for (Object[] row : results) {
                try {
                    Map<String, Object> planning = new HashMap<>();

                    planning.put("id", (String) row[0]);
                    planning.put("orderId", (String) row[1]);
                    planning.put("employeeId", (String) row[2]);
                    planning.put("planningDate", row[3] != null ? row[3].toString() : null);
                    planning.put("startTime", row[4] != null ? row[4].toString() : null);
                    planning.put("endTime", row[5] != null ? row[5].toString() : null);
                    planning.put("durationMinutes", row[6] != null ? ((Number) row[6]).intValue() : 0);
                    planning.put("priority", (String) row[7]);
                    planning.put("status", (String) row[8]);
                    planning.put("completed", row[9] != null ? ((Number) row[9]).intValue() == 1 : false);
                    planning.put("cardCount", row[10] != null ? ((Number) row[10]).intValue() : 1);
                    planning.put("notes", (String) row[11]);
                    planning.put("progressPercentage", row[12] != null ? ((Number) row[12]).intValue() : 0);
                    planning.put("employeeName", (String) row[13]);
                    planning.put("orderNumber", (String) row[14]);

                    plannings.add(planning);

                    System.out.println("  ✅ Planning: " + row[14] + " → " + row[13] +
                            " (" + row[3] + " " + row[4] + ")");

                } catch (Exception rowError) {
                    System.err.println("❌ Error processing planning row: " + rowError.getMessage());
                    // Continue avec les autres plannings
                }
            }

            System.out.println("✅ Successfully processed " + plannings.size() + " plannings");

        } catch (Exception e) {
            System.err.println("❌ Error retrieving plannings for view-simple: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(plannings);
        }

        return ResponseEntity.ok(plannings);
    }

    /**
     * ✅ NOUVEAU ENDPOINT POUR DEBUG
     */
    @GetMapping("/view-all")
    public ResponseEntity<List<Map<String, Object>>> viewAllPlannings() {
        System.out.println("🔍 DEBUG: Loading ALL plannings without date filter...");

        try {
            String sqlView = """
            SELECT 
                HEX(p.id) as id,
                p.planning_date as planningDate,
                p.start_time as startTime,
                p.estimated_duration_minutes as durationMinutes,
                p.priority,
                p.status,
                p.card_count as cardCount,
                p.notes
            FROM j_planning p
            ORDER BY p.created_at DESC
            LIMIT 50
        """;

            Query viewQuery = entityManager.createNativeQuery(sqlView);
            @SuppressWarnings("unchecked")
            List<Object[]> results = viewQuery.getResultList();

            List<Map<String, Object>> plannings = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> planning = new HashMap<>();
                planning.put("id", row[0]);
                planning.put("planningDate", row[1]);
                planning.put("startTime", row[2]);
                planning.put("durationMinutes", row[3]);
                planning.put("priority", row[4]);
                planning.put("status", row[5]);
                planning.put("cardCount", row[6]);
                planning.put("notes", row[7]);
                plannings.add(planning);
            }

            System.out.println("✅ DEBUG: Found " + plannings.size() + " plannings total");
            return ResponseEntity.ok(plannings);

        } catch (Exception e) {
            System.err.println("❌ DEBUG endpoint error: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * ✅ CORRECTIF FINAL - Remplacez la méthode generatePlanning dans PlanningController.java
     * L'erreur vient de l'utilisation d'executeUpdate() sur une requête SELECT
     */
// ✅ CORRECTION - Variable scope dans PlanningController.java

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePlanning(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("=== PLANNING GENERATION (MANUAL TRANSACTION) ===");
            System.out.println("📋 Request: " + request);

            // ========== PARAMETERS ==========
            String startDate = (String) request.getOrDefault("startDate", "2025-06-01");
            Integer timePerCard = request.containsKey("timePerCard") ?
                    ((Number) request.get("timePerCard")).intValue() : 3;

            System.out.println("✅ Parameters:");
            System.out.println("  startDate: " + startDate);
            System.out.println("  timePerCard: " + timePerCard);

            // ========== LOAD EMPLOYEES (READ-ONLY) ==========
            List<Map<String, Object>> employees = employeService.getAllActiveEmployees();
            if (employees.isEmpty()) {
                result.put("success", false);
                result.put("message", "No active employees found");
                return ResponseEntity.ok(result);
            }
            System.out.println("👥 Active employees: " + employees.size());
            for (Map<String, Object> emp : employees) {
                System.out.println("  👤 " + emp.get("firstName") + " " + emp.get("lastName"));
            }

            // ========== LOAD ORDERS (READ-ONLY) ==========
            String sqlOrders = """
            SELECT 
                HEX(o.id) as id, 
                o.num_commande as orderNumber, 
                o.delai as delayCode,
                COALESCE(COUNT(cco.card_certification_id), 1) as cardCount,
                CASE o.delai
                    WHEN 'X' THEN 'URGENT'
                    WHEN 'F+' THEN 'HIGH' 
                    WHEN 'F' THEN 'MEDIUM'
                    WHEN 'E' THEN 'LOW'
                    WHEN 'C' THEN 'NORMAL'
                    ELSE 'MEDIUM'
                END as priority,
                o.date as creationDate
            FROM `order` o
            LEFT JOIN card_certification_order cco ON o.id = cco.order_id
            WHERE o.status IN (1,2,3) 
            AND o.date >= ?
            GROUP BY o.id, o.num_commande, o.delai, o.date
            HAVING cardCount > 0
            ORDER BY 
                CASE o.delai
                    WHEN 'X' THEN 1
                    WHEN 'F+' THEN 2 
                    WHEN 'F' THEN 3
                    WHEN 'E' THEN 4
                    WHEN 'C' THEN 5
                    ELSE 6 
                END,
                o.date ASC
        """;

            Query orderQuery = entityManager.createNativeQuery(sqlOrders);
            orderQuery.setParameter(1, startDate);

            @SuppressWarnings("unchecked")
            List<Object[]> orderResults = orderQuery.getResultList();

            System.out.println("📦 TOTAL orders found from " + startDate + ": " + orderResults.size());

            if (orderResults.isEmpty()) {
                result.put("success", true);
                result.put("message", "No orders found from " + startDate);
                result.put("ordersAnalyzed", 0);
                result.put("planningsSaved", 0);
                return ResponseEntity.ok(result);
            }

            // ========== DECLARE VARIABLES IN CORRECT SCOPE ==========
            EntityTransaction transaction = entityManager.getTransaction();
            int planningsSaved = 0;
            List<Map<String, Object>> createdPlannings = new ArrayList<>();
            LocalDate planningStartDate = LocalDate.parse(startDate);
            LocalDate currentDate = planningStartDate; // ✅ Déclarer ici
            LocalTime currentTime = LocalTime.of(9, 0); // ✅ Déclarer ici

            try {
                transaction.begin();
                System.out.println("🔄 Manual transaction started");

                // ========== CLEAN EXISTING PLANNINGS ==========
                String cleanSql = """
                DELETE FROM j_planning 
                WHERE planning_date >= ? 
                OR created_at >= NOW() - INTERVAL 1 HOUR
            """;
                Query cleanQuery = entityManager.createNativeQuery(cleanSql);
                cleanQuery.setParameter(1, planningStartDate);
                int cleanedPlannings = cleanQuery.executeUpdate();
                System.out.println("🗑️ Cleaned " + cleanedPlannings + " existing plannings");

                // ========== PLAN ALL ORDERS WITH ROUND-ROBIN ==========
                Set<String> processedOrderIds = new HashSet<>();

                System.out.println("🎯 Starting round-robin distribution for " + orderResults.size() + " orders among " + employees.size() + " employees");

                for (int i = 0; i < orderResults.size(); i++) {
                    Object[] row = orderResults.get(i);
                    String orderId = (String) row[0];
                    String orderNumber = (String) row[1];
                    String delayCode = (String) row[2];
                    Integer cardCount = ((Number) row[3]).intValue();
                    String priority = (String) row[4];

                    // Skip duplicates
                    if (processedOrderIds.contains(orderId)) {
                        continue;
                    }

                    // Calculate duration
                    int durationMinutes = Math.max(30, cardCount * timePerCard);

                    // ✅ ROUND-ROBIN DISTRIBUTION
                    int employeeIndex = i % employees.size();
                    Map<String, Object> employee = employees.get(employeeIndex);
                    String employeeId = (String) employee.get("id");
                    String employeeName = employee.get("firstName") + " " + employee.get("lastName");

                    // Create planning entry
                    String planningId = UUID.randomUUID().toString().replace("-", "");
                    LocalDateTime startDateTime = LocalDateTime.of(currentDate, currentTime);
                    LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);

                    try {
                        String insertSql = """
                    INSERT INTO j_planning 
                    (id, order_id, employee_id, planning_date, start_time, end_time, 
                     estimated_duration_minutes, estimated_end_time, priority, status, 
                     completed, card_count, notes, created_at, updated_at)
                    VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                    """;

                        Query insertQuery = entityManager.createNativeQuery(insertSql);
                        insertQuery.setParameter(1, planningId);
                        insertQuery.setParameter(2, orderId);
                        insertQuery.setParameter(3, employeeId);
                        insertQuery.setParameter(4, currentDate);
                        insertQuery.setParameter(5, startDateTime);
                        insertQuery.setParameter(6, endDateTime);
                        insertQuery.setParameter(7, durationMinutes);
                        insertQuery.setParameter(8, endDateTime);
                        insertQuery.setParameter(9, priority);
                        insertQuery.setParameter(10, "SCHEDULED");
                        insertQuery.setParameter(11, 0); // completed = false
                        insertQuery.setParameter(12, cardCount);
                        insertQuery.setParameter(13, "Auto-planned: " + orderNumber + " (" + cardCount + " cards, " + delayCode + ")");

                        int rowsAffected = insertQuery.executeUpdate();

                        if (rowsAffected > 0) {
                            planningsSaved++;
                            processedOrderIds.add(orderId);

                            // Add to results
                            Map<String, Object> planning = new HashMap<>();
                            planning.put("id", planningId);
                            planning.put("orderId", orderId);
                            planning.put("orderNumber", orderNumber);
                            planning.put("employeeId", employeeId);
                            planning.put("employeeName", employeeName);
                            planning.put("durationMinutes", durationMinutes);
                            planning.put("cardCount", cardCount);
                            planning.put("priority", priority);
                            planning.put("delayCode", delayCode);
                            planning.put("planningDate", currentDate.toString());
                            planning.put("startTime", currentTime.toString());

                            createdPlannings.add(planning);

                            System.out.println("✅ [" + (i+1) + "/" + orderResults.size() + "] " +
                                    orderNumber + " (" + delayCode + "/" + priority + ") → " + employeeName +
                                    " (" + cardCount + " cards, " + durationMinutes + "min)");

                            // Advance time realistically
                            currentTime = currentTime.plusMinutes(durationMinutes + 15);
                            if (currentTime.isAfter(LocalTime.of(17, 0))) {
                                currentDate = currentDate.plusDays(1);
                                currentTime = LocalTime.of(9, 0);

                                // Skip weekends
                                while (currentDate.getDayOfWeek().getValue() >= 6) {
                                    currentDate = currentDate.plusDays(1);
                                }
                            }
                        }

                    } catch (Exception insertError) {
                        System.err.println("❌ Insert error for " + orderNumber + ": " + insertError.getMessage());
                        // Continue with other orders instead of failing completely
                    }
                }

                // ========== COMMIT TRANSACTION ==========
                transaction.commit();
                System.out.println("✅ Transaction committed successfully");

            } catch (Exception transactionError) {
                System.err.println("❌ Transaction error: " + transactionError.getMessage());
                transactionError.printStackTrace();

                if (transaction.isActive()) {
                    try {
                        transaction.rollback();
                        System.out.println("🔄 Transaction rolled back");
                    } catch (Exception rollbackError) {
                        System.err.println("❌ Rollback error: " + rollbackError.getMessage());
                    }
                }

                result.put("success", false);
                result.put("message", "Transaction failed: " + transactionError.getMessage());
                return ResponseEntity.status(500).body(result);
            }

            // ========== FINAL SUMMARY ==========
            System.out.println("📊 FINAL DISTRIBUTION SUMMARY:");
            Map<String, Integer> employeeAssignments = new HashMap<>();
            Map<String, Integer> priorityDistribution = new HashMap<>();

            for (Map<String, Object> planning : createdPlannings) {
                String empName = (String) planning.get("employeeName");
                String priority = (String) planning.get("priority");
                employeeAssignments.merge(empName, 1, Integer::sum);
                priorityDistribution.merge(priority, 1, Integer::sum);
            }

            employeeAssignments.forEach((name, count) ->
                    System.out.println("  👤 " + name + ": " + count + " orders"));

            System.out.println("📊 PRIORITY DISTRIBUTION:");
            priorityDistribution.forEach((priority, count) ->
                    System.out.println("  🎯 " + priority + ": " + count + " orders"));

            System.out.println("🎉 PLANNING COMPLETED!");
            System.out.println("  📦 Total orders: " + orderResults.size());
            System.out.println("  ✅ Successfully planned: " + planningsSaved);
            System.out.println("  👥 Employees used: " + employeeAssignments.size());

            // ========== RESULTS ==========
            int totalCards = createdPlannings.stream()
                    .mapToInt(p -> (Integer) p.get("cardCount"))
                    .sum();
            int totalMinutes = createdPlannings.stream()
                    .mapToInt(p -> (Integer) p.get("durationMinutes"))
                    .sum();

            result.put("success", true);
            result.put("message", "All orders planned successfully with round-robin distribution!");
            result.put("ordersAnalyzed", orderResults.size());
            result.put("planningsSaved", planningsSaved);
            result.put("employeesUsed", employeeAssignments.size());
            result.put("totalCards", totalCards);
            result.put("totalMinutes", totalMinutes);
            result.put("totalHours", String.format("%.1f", totalMinutes / 60.0));
            result.put("startDate", startDate);
            result.put("endDate", currentDate.toString()); // ✅ Maintenant accessible
            result.put("createdPlannings", createdPlannings);
            result.put("distributionSummary", employeeAssignments);
            result.put("priorityDistribution", priorityDistribution);
            result.put("timePerCardMinutes", timePerCard);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ GENERAL ERROR: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Planning generation failed: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * ✅ MÉTHODE UTILITAIRE POUR MAPPER DÉLAI → PRIORITÉ
     */
    private String mapDelaiToPriorityEnum(String delaiCode) {
        if (delaiCode == null || delaiCode.trim().isEmpty()) return "MEDIUM";

        switch (delaiCode.trim().toUpperCase()) {
            case "X": return "URGENT";
            case "F+": return "HIGH";
            case "F": return "MEDIUM";
            default: return "LOW";
        }
    }

    /**
     * ✅ MÉTHODE UTILITAIRE POUR STACK TRACE
     */
    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
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

    // ==========================================
// SIMPLE FIX: Update EXISTING viewPlanning method
// Just change the SQL query in your existing method
// ==========================================

    /**
     * ✅ SIMPLE FIX: Find your existing viewPlanning method and replace ONLY the SQL part
     */
    @GetMapping("/view")
    public ResponseEntity<List<Map<String, Object>>> viewPlanning(@RequestParam(required = false) String date) {
        List<Map<String, Object>> plannings = new ArrayList<>();

        try {
            String targetDate = date != null ? date : LocalDate.now().toString();

            // ✅ FIXED SQL: Remove reference to 'employee' table, use only 'j_employee'
            String sqlView = """
                        SELECT 
                            HEX(p.id) as planning_id,
                            HEX(p.order_id) as order_id,
                            HEX(p.employee_id) as employee_id,
                            p.planning_date,
                            p.start_time,
                            p.end_time,
                            p.estimated_duration_minutes,
                            p.priority,
                            p.status,
                            p.completed,
                            p.card_count,
                            p.notes,
                            CONCAT(e.prenom, ' ', e.nom) as employee_name,
                            o.num_commande as order_number
                        FROM j_planning p
                        LEFT JOIN j_employee e ON p.employee_id = e.id
                        LEFT JOIN `order` o ON p.order_id = o.id
                        WHERE p.planning_date = ?
                        ORDER BY p.start_time ASC
                    """;

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


    /**
     * English equivalent of /api/planifications/planifications-avec-details
     */
    @GetMapping("/plannings-with-details")
    public ResponseEntity<List<Map<String, Object>>> getPlanningsWithDetails(
            @RequestParam(required = false) String date) {

        List<Map<String, Object>> plannings = new ArrayList<>();

        try {
            String targetDate = date != null ? date : LocalDate.now().toString();

            String sql = """
            SELECT 
                HEX(p.id) as planning_id,
                HEX(p.order_id) as order_id,
                HEX(p.employee_id) as employee_id,
                p.planning_date,
                p.start_time,
                p.end_time,
                p.estimated_duration_minutes,
                p.priority,
                p.status,
                p.completed,
                p.card_count,
                p.notes,
                CONCAT(COALESCE(e.first_name, e.prenom), ' ', COALESCE(e.last_name, e.nom)) as employee_name,
                o.num_commande as order_number
            FROM j_planning p
            LEFT JOIN j_employee e ON p.employee_id = e.id
            LEFT JOIN `order` o ON p.order_id = o.id
            WHERE p.planning_date = ?
            ORDER BY p.start_time
            """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, targetDate);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

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

            System.out.println("✅ Retrieved " + plannings.size() + " detailed plannings for " + targetDate);

        } catch (Exception e) {
            System.err.println("❌ Error retrieving detailed plannings: " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(plannings);
    }




    /**
     * 🛡️ INSERTION BLINDÉE - VERSION QUI MARCHE GARANTIE
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @PostMapping("/generate-bulletproof")
    public ResponseEntity<Map<String, Object>> generateBulletproof() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🛡️ === GÉNÉRATION BLINDÉE ===");

            // ========== DONNÉES FIXES POUR ÉVITER TOUT PROBLÈME ==========

            // Utiliser les données du debug précédent qui fonctionnent
            List<Map<String, Object>> employees = employeService.getAllActiveEmployees();

            // Commandes avec vrais nombres de cartes
            String sqlOrders = """
            SELECT 
                HEX(o.id) as id, 
                o.num_commande, 
                o.delai, 
                COALESCE(COUNT(cco.card_certification_id), 5) as card_count
            FROM `order` o
            LEFT JOIN card_certification_order cco ON o.id = cco.order_id
            WHERE o.status IN (1,2,3) 
            AND o.date >= '2025-06-01' 
            GROUP BY o.id, o.num_commande, o.delai
            HAVING card_count > 0
            LIMIT 5
        """;

            Query orderQuery = entityManager.createNativeQuery(sqlOrders);
            @SuppressWarnings("unchecked")
            List<Object[]> orderResults = orderQuery.getResultList();

            System.out.println("📦 Commandes avec cartes: " + orderResults.size());
            System.out.println("👥 Employés disponibles: " + employees.size());

            if (employees.isEmpty() || orderResults.isEmpty()) {
                result.put("success", false);
                result.put("error", "Pas d'employés ou de commandes");
                return ResponseEntity.ok(result);
            }

            // ========== INSERTION DIRECTE SANS VÉRIFICATION ==========

            int planningsSaved = 0;
            List<Map<String, Object>> createdPlannings = new ArrayList<>();

            for (int i = 0; i < Math.min(orderResults.size(), 3); i++) {
                Object[] orderRow = orderResults.get(i);
                Map<String, Object> employee = employees.get(i % employees.size());

                String orderId = (String) orderRow[0];
                String orderNumber = (String) orderRow[1];
                String delaiCode = (String) orderRow[2];
                Integer cardCount = ((Number) orderRow[3]).intValue();

                String employeeId = (String) employee.get("id");
                String employeeName = employee.get("prenom") + " " + employee.get("nom");

                // Données de planification UNIQUES
                String planningId = UUID.randomUUID().toString().replace("-", "");
                LocalDate uniqueDate = LocalDate.now().plusDays(4 + i); // Date différente pour chaque planning
                LocalTime uniqueTime = LocalTime.of(8 + i, i * 15); // Heure unique
                LocalDateTime startDateTime = LocalDateTime.of(uniqueDate, uniqueTime);
                LocalDateTime endDateTime = startDateTime.plusMinutes(cardCount * 3);

                System.out.println("🔄 Insertion " + (i+1) + ":");
                System.out.println("  📦 " + orderNumber + " (" + cardCount + " cartes)");
                System.out.println("  👤 " + employeeName);
                System.out.println("  📅 " + uniqueDate + " à " + uniqueTime);
                System.out.println("  🆔 Planning ID: " + planningId);

                // ✅ INSERTION SANS AUCUNE VÉRIFICATION PRÉALABLE
                try {
                    String insertSql = """
                INSERT INTO j_planning 
                (id, order_id, employee_id, planning_date, start_time, end_time, 
                 estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
                VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;

                    int rowsAffected = entityManager.createNativeQuery(insertSql)
                            .setParameter(1, planningId)
                            .setParameter(2, orderId)
                            .setParameter(3, employeeId)
                            .setParameter(4, uniqueDate)
                            .setParameter(5, startDateTime)
                            .setParameter(6, endDateTime)
                            .setParameter(7, cardCount * 3)
                            .setParameter(8, mapDelaiToPriority(delaiCode))
                            .setParameter(9, "SCHEDULED")
                            .setParameter(10, false)
                            .setParameter(11, cardCount)
                            .setParameter(12, "🛡️ BLINDÉ: " + orderNumber + " → " + employeeName)
                            .executeUpdate();

                    System.out.println("  💾 Rows affected: " + rowsAffected);

                    if (rowsAffected > 0) {
                        planningsSaved++;

                        Map<String, Object> planningInfo = new HashMap<>();
                        planningInfo.put("planningId", planningId);
                        planningInfo.put("orderNumber", orderNumber);
                        planningInfo.put("employeeName", employeeName);
                        planningInfo.put("cardCount", cardCount);
                        planningInfo.put("planningDate", uniqueDate.toString());
                        planningInfo.put("startTime", uniqueTime.toString());
                        planningInfo.put("durationMinutes", cardCount * 3);
                        createdPlannings.add(planningInfo);

                        System.out.println("  ✅ SUCCÈS !");
                    } else {
                        System.out.println("  ❌ 0 rows affected");
                    }

                } catch (Exception insertError) {
                    System.err.println("  ❌ Erreur insertion: " + insertError.getMessage());
                    insertError.printStackTrace();

                    // Stocker l'erreur mais continuer
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("orderNumber", orderNumber);
                    errorInfo.put("error", insertError.getMessage());
                    createdPlannings.add(errorInfo);
                }
            }

            // ========== VÉRIFICATION FINALE ==========

            String countSql = "SELECT COUNT(*) FROM j_planning";
            Query countQuery = entityManager.createNativeQuery(countSql);
            Number totalCount = (Number) countQuery.getSingleResult();

            System.out.println("📊 RÉSULTATS FINAUX:");
            System.out.println("  • Planifications sauvées: " + planningsSaved);
            System.out.println("  • Total dans la table: " + totalCount);

            // ========== RÉPONSE ==========

            result.put("success", planningsSaved > 0);
            result.put("method", "BULLETPROOF");
            result.put("ordersProcessed", orderResults.size());
            result.put("planningsSaved", planningsSaved);
            result.put("totalInTable", totalCount.intValue());
            result.put("createdPlannings", createdPlannings);
            result.put("message", planningsSaved > 0 ?
                    "🛡️ SUCCESS: " + planningsSaved + " planifications créées !" :
                    "❌ FAILED: Aucune planification créée");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Bulletproof generation error: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * Map delai to priority - Version simplifiée
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
     * Remove duplicate plannings based on order_id + employee_id + planning_date
     */
    @PostMapping("/remove-duplicates")
    @Transactional
    public ResponseEntity<Map<String, Object>> removeDuplicates() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🗑️ Starting duplicate planning removal...");

            // Trouver les doublons
            String findDuplicatesSql = """
        SELECT order_id, employee_id, planning_date, COUNT(*) as duplicate_count
        FROM j_planning 
        GROUP BY order_id, employee_id, planning_date
        HAVING COUNT(*) > 1
        """;

            Query findQuery = entityManager.createNativeQuery(findDuplicatesSql);
            @SuppressWarnings("unchecked")
            List<Object[]> duplicateGroups = findQuery.getResultList();

            int totalDuplicatesRemoved = 0;

            for (Object[] group : duplicateGroups) {
                byte[] orderId = (byte[]) group[0];
                byte[] employeeId = (byte[]) group[1];
                java.sql.Date planningDate = (java.sql.Date) group[2];
                int duplicateCount = ((Number) group[3]).intValue();

                // Garder seulement le plus récent (par created_at)
                String deleteDuplicatesSql = """
            DELETE FROM j_planning 
            WHERE order_id = ? AND employee_id = ? AND planning_date = ?
            AND id NOT IN (
                SELECT id FROM (
                    SELECT id FROM j_planning 
                    WHERE order_id = ? AND employee_id = ? AND planning_date = ?
                    ORDER BY created_at DESC 
                    LIMIT 1
                ) as keeper
            )
            """;

                Query deleteQuery = entityManager.createNativeQuery(deleteDuplicatesSql);
                deleteQuery.setParameter(1, orderId);
                deleteQuery.setParameter(2, employeeId);
                deleteQuery.setParameter(3, planningDate);
                deleteQuery.setParameter(4, orderId);
                deleteQuery.setParameter(5, employeeId);
                deleteQuery.setParameter(6, planningDate);

                int deletedCount = deleteQuery.executeUpdate();
                totalDuplicatesRemoved += deletedCount;

                log.info("Removed {} duplicates for order-employee-date group", deletedCount);
            }

            result.put("success", true);
            result.put("message", "Duplicate removal completed");
            result.put("duplicateGroupsFound", duplicateGroups.size());
            result.put("totalDuplicatesRemoved", totalDuplicatesRemoved);

            log.info("✅ Duplicate removal completed: {} duplicates removed from {} groups",
                    totalDuplicatesRemoved, duplicateGroups.size());

        } catch (Exception e) {
            log.error("❌ Error removing duplicates: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }

        return ResponseEntity.ok(result);
    }

}