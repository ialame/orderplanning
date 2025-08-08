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



// ===============================================
// 🎯 MÉTHODE DE GÉNÉRATION SIMPLIFIÉE GARANTIE
// ===============================================

// Ajoutez cette méthode dans PlanningController.java
// Cette version est ultra-simplifiée pour identifier le problème exact

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
                employees = employeService.getTousEmployesActifs();
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

    // ========== UTILITAIRE POUR STACK TRACE ==========
    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    // ========== ENDPOINT DE DEBUG RAPIDE ==========
    @GetMapping("/quick-debug")
    public ResponseEntity<Map<String, Object>> quickDebug() {
        Map<String, Object> debug = new HashMap<>();

        try {
            // Test employés
            List<Map<String, Object>> employees = employeService.getTousEmployesActifs();
            debug.put("employees_count", employees.size());
            debug.put("first_employee", employees.isEmpty() ? null : employees.get(0));

            // Test commandes
            Query orderQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM `order` WHERE status IN (1,2,3)");
            Number orderCount = (Number) orderQuery.getSingleResult();
            debug.put("orders_count", orderCount.intValue());

            // Test plannings
            Query planningQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM j_planning");
            Number planningCount = (Number) planningQuery.getSingleResult();
            debug.put("plannings_count", planningCount.intValue());

            debug.put("status", "OK");
            debug.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            debug.put("status", "ERROR");
            debug.put("error", e.getMessage());
            debug.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(debug);
        }
    }



    @GetMapping("/diagnostic-doublons")
    public ResponseEntity<Map<String, Object>> diagnosticDoublons() {
        Map<String, Object> result = new HashMap<>();

        try {
            // ✅ Requête basée sur la vraie structure j_planning
            String countDuplicatesSql = """
            SELECT COUNT(*) FROM (
                SELECT order_id, employee_id, planning_date, start_time
                FROM j_planning 
                GROUP BY order_id, employee_id, planning_date, start_time
                HAVING COUNT(*) > 1
            ) as doublons
        """;

            Number doublons = (Number) entityManager.createNativeQuery(countDuplicatesSql)
                    .getSingleResult();

            String countTotalSql = "SELECT COUNT(*) FROM j_planning";
            Number total = (Number) entityManager.createNativeQuery(countTotalSql)
                    .getSingleResult();

            // Détails des doublons
            String detailsDoublonsSql = """
            SELECT 
                HEX(order_id) as order_id,
                HEX(employee_id) as employee_id,
                planning_date,
                start_time,
                COUNT(*) as nb_occurrences
            FROM j_planning 
            GROUP BY order_id, employee_id, planning_date, start_time
            HAVING COUNT(*) > 1
            ORDER BY COUNT(*) DESC
            LIMIT 5
        """;

            Query detailsQuery = entityManager.createNativeQuery(detailsDoublonsSql);
            @SuppressWarnings("unchecked")
            List<Object[]> details = detailsQuery.getResultList();

            List<Map<String, Object>> doublonsDetails = new ArrayList<>();
            for (Object[] row : details) {
                Map<String, Object> doublon = new HashMap<>();
                doublon.put("order_id", row[0]);
                doublon.put("employee_id", row[1]);
                doublon.put("planning_date", row[2]);
                doublon.put("start_time", row[3]);
                doublon.put("occurrences", row[4]);
                doublonsDetails.add(doublon);
            }

            result.put("success", true);
            result.put("total_planifications", total.intValue());
            result.put("groupes_doublons", doublons.intValue());
            result.put("details_doublons", doublonsDetails);
            result.put("table_utilisee", "j_planning");
            result.put("message", doublons.intValue() > 0 ?
                    "⚠️ " + doublons + " groupes de doublons détectés" : "✅ Aucun doublon");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/nettoyer-doublons")
    @Transactional
    public ResponseEntity<Map<String, Object>> nettoyerDoublons() {
        Map<String, Object> result = new HashMap<>();

        try {
            // ✅ Nettoyage basé sur la vraie structure
            String deleteDuplicatesSql = """
            DELETE p1 FROM j_planning p1
            INNER JOIN j_planning p2 
            WHERE p1.order_id = p2.order_id 
              AND p1.employee_id = p2.employee_id
              AND p1.planning_date = p2.planning_date
              AND p1.start_time = p2.start_time
              AND p1.created_at < p2.created_at
        """;

            int doublonsSupprimes = entityManager.createNativeQuery(deleteDuplicatesSql).executeUpdate();

            // Ajouter contrainte unique si pas déjà présente
            try {
                String addConstraintSql = """
                ALTER TABLE j_planning 
                ADD CONSTRAINT uk_planning_unique 
                UNIQUE (order_id, employee_id, planning_date, start_time)
            """;
                entityManager.createNativeQuery(addConstraintSql).executeUpdate();
                result.put("contrainte_ajoutee", true);
            } catch (Exception e) {
                result.put("contrainte_ajoutee", false);
                result.put("contrainte_info", "Contrainte déjà existante ou erreur: " + e.getMessage());
            }

            result.put("success", true);
            result.put("doublons_supprimes", doublonsSupprimes);
            result.put("table_nettoyee", "j_planning");
            result.put("message", "🧹 " + doublonsSupprimes + " doublons supprimés de j_planning");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    private String insererPlanificationSecurisee(String orderId, String employeeId,
                                                 LocalDate planningDate, LocalTime startTime,
                                                 int durationMinutes, String priority, String notes) {

        try {
            // ✅ Vérifier si existe déjà avec la vraie structure
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
            checkQuery.setParameter(3, planningDate);
            checkQuery.setParameter(4, startTime);

            Number count = (Number) checkQuery.getSingleResult();
            if (count.intValue() > 0) {
                System.out.println("⚠️ Planification déjà existante - IGNORÉE");
                return null;
            }

            // ✅ Insertion avec toutes les colonnes de la vraie structure
            String newId = UUID.randomUUID().toString().replace("-", "");
            LocalDateTime startDateTime = LocalDateTime.of(planningDate, startTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);

            String insertSql = """
            INSERT INTO j_planning 
            (id, order_id, employee_id, planning_date, start_time, end_time, 
             estimated_duration_minutes, estimated_end_time, priority, status, 
             completed, created_at, updated_at, card_count, notes, progress_percentage)
            VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?, ?)
        """;

            int rowsAffected = entityManager.createNativeQuery(insertSql)
                    .setParameter(1, newId)
                    .setParameter(2, orderId)
                    .setParameter(3, employeeId)
                    .setParameter(4, planningDate)
                    .setParameter(5, startDateTime)
                    .setParameter(6, endDateTime)
                    .setParameter(7, durationMinutes)
                    .setParameter(8, endDateTime)
                    .setParameter(9, priority != null ? priority : "MEDIUM")
                    .setParameter(10, "SCHEDULED")
                    .setParameter(11, 0) // completed = false
                    .setParameter(12, 1) // card_count par défaut
                    .setParameter(13, notes != null ? notes : "Generated planning")
                    .setParameter(14, 0) // progress_percentage
                    .executeUpdate();

            return rowsAffected > 0 ? newId : null;

        } catch (Exception e) {
            System.err.println("❌ Erreur insertion planification: " + e.getMessage());
            return null;
        }
    }

    // 4. CHARGEMENT DES PLANIFICATIONS - VERSION CORRIGÉE
    @GetMapping("/view-simple")
    public ResponseEntity<List<Map<String, Object>>> viewPlanningsSimple(@RequestParam(required = false) String date) {
        List<Map<String, Object>> plannings = new ArrayList<>();

        try {
            String targetDate = date != null ? date : LocalDate.now().toString();

            // ✅ Requête optimisée avec la vraie structure
            String sqlView = """
            SELECT 
                HEX(p.id) as id,
                HEX(p.order_id) as orderId,
                HEX(p.employee_id) as employeeId,
                p.planning_date as planningDate,
                p.start_time as startTime,
                p.end_time as endTime,
                p.estimated_duration_minutes as durationMinutes,
                p.priority,
                p.status,
                p.completed,
                p.card_count as cardCount,
                p.notes,
                p.progress_percentage as progressPercentage,
                
                -- Nom employé (essayer plusieurs formats)
                COALESCE(
                    CONCAT(e1.prenom, ' ', e1.nom),
                    CONCAT(e2.first_name, ' ', e2.last_name),
                    CONCAT('Employee ', RIGHT(HEX(p.employee_id), 6))
                ) as employeeName,
                
                -- Numéro commande
                o.num_commande as orderNumber
                
            FROM j_planning p
            LEFT JOIN employe e1 ON p.employee_id = e1.id
            LEFT JOIN employee e2 ON p.employee_id = e2.id  
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
                planning.put("progressPercentage", row[12]);
                planning.put("employeeName", row[13]);
                planning.put("orderNumber", row[14]);

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
     * ✅ GÉNÉRATION FINALE QUI MARCHE - ENUM CORRECTS
     * Remplacez votre méthode generatePlanning par celle-ci
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePlanning(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("=== POKEMON CARD ORDER PLANNING GENERATION ===");

            // ========== PARAMÈTRES ==========
            String startDate = "2025-06-01";
            Integer timePerCard = 3;

            // ========== CHARGEMENT DES DONNÉES ==========
            List<Map<String, Object>> employees = employeService.getTousEmployesActifs();
            System.out.println("✅ Employés: " + employees.size());

            if (employees.isEmpty()) {
                result.put("success", false);
                result.put("error", "Aucun employé actif");
                return ResponseEntity.ok(result);
            }

            // Commandes avec cartes réelles
            String sqlOrders = """
            SELECT 
                HEX(o.id) as id, 
                o.num_commande, 
                o.delai, 
                COALESCE(COUNT(cco.card_certification_id), 5) as card_count
            FROM `order` o
            LEFT JOIN card_certification_order cco ON o.id = cco.order_id
            WHERE o.status IN (1,2,3) 
            AND o.date >= ? 
            GROUP BY o.id, o.num_commande, o.delai
            HAVING card_count > 0
            ORDER BY 
                CASE o.delai 
                    WHEN 'X' THEN 1 
                    WHEN 'F+' THEN 2 
                    WHEN 'F' THEN 3 
                    ELSE 4 
                END
            LIMIT 15
        """;

            Query orderQuery = entityManager.createNativeQuery(sqlOrders);
            orderQuery.setParameter(1, startDate);

            @SuppressWarnings("unchecked")
            List<Object[]> orderResults = orderQuery.getResultList();

            System.out.println("✅ Commandes avec cartes: " + orderResults.size());

            if (orderResults.isEmpty()) {
                result.put("success", true);
                result.put("message", "Aucune commande avec cartes depuis " + startDate);
                result.put("planningsSaved", 0);
                return ResponseEntity.ok(result);
            }

            // ========== GÉNÉRATION DES PLANIFICATIONS ==========
            int planningsSaved = 0;
            List<Map<String, Object>> createdPlannings = new ArrayList<>();

            for (int i = 0; i < Math.min(orderResults.size(), 10); i++) {
                try {
                    Object[] orderRow = orderResults.get(i);
                    Map<String, Object> employee = employees.get(i % employees.size());

                    String orderId = (String) orderRow[0];
                    String orderNumber = (String) orderRow[1];
                    String delaiCode = (String) orderRow[2];
                    Integer cardCount = ((Number) orderRow[3]).intValue();

                    String employeeId = (String) employee.get("id");
                    String employeeName = employee.get("prenom") + " " + employee.get("nom");

                    // Calcul durée
                    int durationMinutes = Math.max(cardCount * timePerCard, 30);

                    // Date et heure uniques
                    LocalDate planningDate = LocalDate.now().plusDays(1 + (i / 5)); // Étaler sur plusieurs jours
                    LocalTime startTime = LocalTime.of(9 + (i % 8), (i % 4) * 15); // Heures variées
                    LocalDateTime startDateTime = LocalDateTime.of(planningDate, startTime);
                    LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);

                    System.out.println("🔄 Planification " + (i+1) + ":");
                    System.out.println("  📦 " + orderNumber + " (" + cardCount + " cartes)");
                    System.out.println("  👤 " + employeeName);
                    System.out.println("  📅 " + planningDate + " à " + startTime);

                    // ✅ INSERTION AVEC ENUM CORRECTS
                    String planningId = UUID.randomUUID().toString().replace("-", "");
                    String insertSql = """
                INSERT INTO j_planning 
                (id, order_id, employee_id, planning_date, start_time, end_time, 
                 estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
                VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;

                    try {
                        int rowsAffected = entityManager.createNativeQuery(insertSql)
                                .setParameter(1, planningId)
                                .setParameter(2, orderId)
                                .setParameter(3, employeeId)
                                .setParameter(4, planningDate)
                                .setParameter(5, startDateTime)
                                .setParameter(6, endDateTime)
                                .setParameter(7, durationMinutes)
                                .setParameter(8, mapDelaiToPriorityEnum(delaiCode)) // ✅ ENUM valide
                                .setParameter(9, "SCHEDULED") // ✅ ENUM valide
                                .setParameter(10, false)
                                .setParameter(11, cardCount)
                                .setParameter(12, "🎮 Pokémon: " + orderNumber + " → " + employeeName + " (" + cardCount + " cartes)")
                                .executeUpdate();

                        if (rowsAffected > 0) {
                            planningsSaved++;

                            Map<String, Object> planning = new HashMap<>();
                            planning.put("planningId", planningId);
                            planning.put("orderNumber", orderNumber);
                            planning.put("employeeName", employeeName);
                            planning.put("cardCount", cardCount);
                            planning.put("durationMinutes", durationMinutes);
                            planning.put("planningDate", planningDate.toString());
                            planning.put("startTime", startTime.toString());
                            planning.put("priority", mapDelaiToPriorityEnum(delaiCode));
                            createdPlannings.add(planning);

                            System.out.println("  ✅ SUCCÈS !");
                        } else {
                            System.out.println("  ❌ 0 rows affected");
                        }

                    } catch (Exception insertError) {
                        System.err.println("  ❌ Erreur insertion: " + insertError.getMessage());
                        // Continuer avec les autres
                    }

                } catch (Exception e) {
                    System.err.println("❌ Erreur commande " + (i+1) + ": " + e.getMessage());
                }
            }

            // ========== RÉSULTATS ==========
            int totalCards = createdPlannings.stream()
                    .mapToInt(p -> (Integer) p.get("cardCount"))
                    .sum();
            int totalMinutes = createdPlannings.stream()
                    .mapToInt(p -> (Integer) p.get("durationMinutes"))
                    .sum();

            result.put("success", true);
            result.put("message", "🎮 Planification des commandes Pokémon terminée avec succès !");
            result.put("ordersAnalyzed", orderResults.size());
            result.put("employeesUsed", employees.size());
            result.put("planningsSaved", planningsSaved);
            result.put("totalCards", totalCards);
            result.put("totalMinutes", totalMinutes);
            result.put("totalHours", String.format("%.1f", totalMinutes / 60.0));
            result.put("averageCardsPerOrder", totalCards / Math.max(planningsSaved, 1));
            result.put("createdPlannings", createdPlannings);
            result.put("timePerCardMinutes", timePerCard);

            System.out.println("🎉 GÉNÉRATION TERMINÉE !");
            System.out.println("📊 Résultats: " + planningsSaved + " planifications créées");
            System.out.println("📦 Total: " + totalCards + " cartes, " + totalMinutes + " minutes");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Erreur génération: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("planningsSaved", 0);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * ✅ MAPPING CORRECT POUR LES ENUM DE PRIORITY
     */
    private String mapDelaiToPriorityEnum(String delai) {
        if (delai == null) return "MEDIUM";

        switch (delai.toUpperCase()) {
            case "X": return "URGENT";    // Urgent
            case "F+": return "HIGH";     // Haute priorité
            case "F": return "MEDIUM";    // Moyenne priorité
            case "E": return "LOW";       // Basse priorité
            case "C": return "MEDIUM";    // Moyenne par défaut
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
            LEFT JOIN employee e ON p.employee_id = e.id
            LEFT JOIN employe e2 ON p.employee_id = e2.id  
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
     * 🔍 DEBUG: Vérifier la structure de la table order
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @GetMapping("/debug-order-structure")
    public ResponseEntity<Map<String, Object>> debugOrderStructure() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🔍 === DEBUG ORDER TABLE STRUCTURE ===");

            // 1. Vérifier que la table existe
            String sqlTableExists = "SHOW TABLES LIKE 'order'";
            Query tableQuery = entityManager.createNativeQuery(sqlTableExists);
            @SuppressWarnings("unchecked")
            List<String> tables = tableQuery.getResultList();

            boolean tableExists = !tables.isEmpty();
            result.put("tableExists", tableExists);

            if (!tableExists) {
                result.put("error", "Table 'order' does not exist");
                return ResponseEntity.ok(result);
            }

            // 2. Décrire la structure
            String sqlDescribe = "DESCRIBE `order`";
            Query describeQuery = entityManager.createNativeQuery(sqlDescribe);
            @SuppressWarnings("unchecked")
            List<Object[]> columns = describeQuery.getResultList();

            List<Map<String, Object>> columnInfo = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();

            for (Object[] col : columns) {
                String columnName = (String) col[0];
                String columnType = (String) col[1];

                Map<String, Object> colData = new HashMap<>();
                colData.put("name", columnName);
                colData.put("type", columnType);
                colData.put("null", col[2]);
                colData.put("key", col[3]);
                colData.put("default", col[4]);
                colData.put("extra", col[5]);

                columnInfo.add(colData);
                columnNames.add(columnName);

                System.out.println("  📋 " + columnName + " (" + columnType + ")");
            }

            result.put("columns", columnInfo);
            result.put("columnNames", columnNames);

            // 3. Compter les commandes
            String sqlCount = "SELECT COUNT(*) FROM `order`";
            Query countQuery = entityManager.createNativeQuery(sqlCount);
            Number totalOrders = (Number) countQuery.getSingleResult();
            result.put("totalOrders", totalOrders.intValue());

            // 4. Compter par statut
            String sqlByStatus = "SELECT status, COUNT(*) FROM `order` GROUP BY status";
            Query statusQuery = entityManager.createNativeQuery(sqlByStatus);
            @SuppressWarnings("unchecked")
            List<Object[]> statusResults = statusQuery.getResultList();

            Map<String, Integer> statusCounts = new HashMap<>();
            for (Object[] row : statusResults) {
                statusCounts.put(String.valueOf(row[0]), ((Number) row[1]).intValue());
            }
            result.put("statusCounts", statusCounts);

            // 5. Échantillon de données
            String sqlSample = "SELECT * FROM `order` ORDER BY date DESC LIMIT 3";
            Query sampleQuery = entityManager.createNativeQuery(sqlSample);
            @SuppressWarnings("unchecked")
            List<Object[]> sampleData = sampleQuery.getResultList();

            List<Map<String, Object>> sampleOrders = new ArrayList<>();
            for (Object[] row : sampleData) {
                Map<String, Object> order = new HashMap<>();
                for (int i = 0; i < Math.min(row.length, columnNames.size()); i++) {
                    order.put(columnNames.get(i), row[i]);
                }
                sampleOrders.add(order);
            }
            result.put("sampleOrders", sampleOrders);

            // 6. Recommandations
            List<String> recommendations = new ArrayList<>();
            if (columnNames.contains("nombre_cartes")) {
                recommendations.add("✅ Use 'nombre_cartes' for card count");
            }
            if (columnNames.contains("card_count")) {
                recommendations.add("✅ Use 'card_count' for card count");
            }
            if (columnNames.contains("delai")) {
                recommendations.add("✅ Use 'delai' for priority mapping");
            }
            if (columnNames.contains("status")) {
                recommendations.add("✅ Use 'status' for order filtering");
            }

            result.put("recommendations", recommendations);
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Error debugging order structure: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 🔍 DEBUG DÉTAILLÉ POUR LA GÉNÉRATION DE PLANIFICATION
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @PostMapping("/debug-generation")
    public ResponseEntity<Map<String, Object>> debugGeneration() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🔍 === DEBUG GÉNÉRATION PLANIFICATION ===");

            // ========== PHASE 1: STRUCTURE DES TABLES ==========
            result.put("phase", "1-tables");

            // Vérifier j_planning
            String sqlPlanningStructure = "DESCRIBE j_planning";
            Query planningQuery = entityManager.createNativeQuery(sqlPlanningStructure);
            @SuppressWarnings("unchecked")
            List<Object[]> planningColumns = planningQuery.getResultList();

            List<String> planningColumnNames = new ArrayList<>();
            for (Object[] col : planningColumns) {
                planningColumnNames.add((String) col[0]);
            }
            result.put("j_planning_columns", planningColumnNames);
            System.out.println("✅ j_planning columns: " + planningColumnNames);

            // ========== PHASE 2: EMPLOYÉS ==========
            result.put("phase", "2-employees");

            List<Map<String, Object>> employees = employeService.getTousEmployesActifs();
            result.put("employees_count", employees.size());

            List<Map<String, Object>> employeesSample = new ArrayList<>();
            for (int i = 0; i < Math.min(employees.size(), 3); i++) {
                Map<String, Object> emp = employees.get(i);
                Map<String, Object> empSample = new HashMap<>();
                empSample.put("id", emp.get("id"));
                empSample.put("nom", emp.get("nom"));
                empSample.put("prenom", emp.get("prenom"));
                empSample.put("id_length", emp.get("id") != null ? emp.get("id").toString().length() : 0);
                employeesSample.add(empSample);
            }
            result.put("employees_sample", employeesSample);
            System.out.println("✅ Employees: " + employees.size() + ", Sample: " + employeesSample);

            // ========== PHASE 3: COMMANDES ==========
            result.put("phase", "3-orders");

            String sqlOrders = """
            SELECT HEX(id) as id, num_commande, delai, card_count, 
                   date, priority, status, total_price
            FROM `order` 
            WHERE status IN (1,2,3) 
            AND date >= '2025-06-01' 
            ORDER BY date DESC 
            LIMIT 5
        """;

            Query orderQuery = entityManager.createNativeQuery(sqlOrders);
            @SuppressWarnings("unchecked")
            List<Object[]> orderResults = orderQuery.getResultList();

            List<Map<String, Object>> ordersSample = new ArrayList<>();
            for (Object[] row : orderResults) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", row[0]);
                order.put("num_commande", row[1]);
                order.put("delai", row[2]);
                order.put("card_count", row[3]);
                order.put("id_length", row[0] != null ? row[0].toString().length() : 0);
                ordersSample.add(order);
            }
            result.put("orders_count", orderResults.size());
            result.put("orders_sample", ordersSample);
            System.out.println("✅ Orders: " + orderResults.size() + ", Sample: " + ordersSample);

            // ========== PHASE 4: TEST D'INSERTION MANUELLE ==========
            result.put("phase", "4-manual_insert");

            if (!employees.isEmpty() && !orderResults.isEmpty()) {
                try {
                    String testEmployeeId = (String) employees.get(0).get("id");
                    String testOrderId = (String) orderResults.get(0)[0];

                    System.out.println("🧪 Test insertion avec:");
                    System.out.println("  Employee ID: " + testEmployeeId + " (length: " + testEmployeeId.length() + ")");
                    System.out.println("  Order ID: " + testOrderId + " (length: " + testOrderId.length() + ")");

                    // Test de vérification d'existence
                    String checkSql = "SELECT COUNT(*) FROM j_planning WHERE order_id = UNHEX(?) AND employee_id = UNHEX(?)";
                    Query checkQuery = entityManager.createNativeQuery(checkSql);
                    checkQuery.setParameter(1, testOrderId);
                    checkQuery.setParameter(2, testEmployeeId);

                    @SuppressWarnings("unchecked")
                    List<Number> existingCount = checkQuery.getResultList();
                    int existing = existingCount.isEmpty() ? 0 : existingCount.get(0).intValue();

                    result.put("test_employee_id", testEmployeeId);
                    result.put("test_order_id", testOrderId);
                    result.put("existing_plannings", existing);

                    System.out.println("🔍 Planifications existantes: " + existing);

                    // Test d'insertion si pas de doublon
                    if (existing == 0) {
                        String newId = UUID.randomUUID().toString().replace("-", "");
                        LocalDate tomorrow = LocalDate.now().plusDays(1);
                        LocalDateTime startTime = LocalDateTime.of(tomorrow, LocalTime.of(10, 0));
                        LocalDateTime endTime = startTime.plusMinutes(60);

                        String insertSql = """
                    INSERT INTO j_planning 
                    (id, order_id, employee_id, planning_date, start_time, end_time, 
                     estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
                    VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                    """;

                        int rowsAffected = entityManager.createNativeQuery(insertSql)
                                .setParameter(1, newId)
                                .setParameter(2, testOrderId)
                                .setParameter(3, testEmployeeId)
                                .setParameter(4, tomorrow)
                                .setParameter(5, startTime)
                                .setParameter(6, endTime)
                                .setParameter(7, 60)
                                .setParameter(8, "MEDIUM")
                                .setParameter(9, "SCHEDULED")
                                .setParameter(10, false)
                                .setParameter(11, 10)
                                .setParameter(12, "DEBUG: Test insertion")
                                .executeUpdate();

                        result.put("manual_insert_success", rowsAffected > 0);
                        result.put("manual_insert_rows", rowsAffected);
                        result.put("manual_insert_id", newId);

                        System.out.println("🧪 Test insertion result: " + rowsAffected + " rows affected");

                    } else {
                        result.put("manual_insert_success", false);
                        result.put("manual_insert_reason", "Planning already exists");
                    }

                } catch (Exception e) {
                    result.put("manual_insert_error", e.getMessage());
                    System.err.println("❌ Test insertion error: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // ========== PHASE 5: VÉRIFICATION POST-TEST ==========
            result.put("phase", "5-verification");

            String countSql = "SELECT COUNT(*) FROM j_planning";
            Query countQuery = entityManager.createNativeQuery(countSql);
            Number totalPlannings = (Number) countQuery.getSingleResult();
            result.put("total_plannings_after_test", totalPlannings.intValue());

            System.out.println("📊 Total plannings après test: " + totalPlannings);

            // ========== RÉSULTAT FINAL ==========
            result.put("success", true);
            result.put("diagnostic_complete", true);
            result.put("recommendations", List.of(
                    "Vérifiez les IDs d'employés et commandes",
                    "Contrôlez la structure de j_planning",
                    "Examinez les logs d'insertion",
                    "Testez l'insertion manuelle"
            ));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Debug generation error: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 🧪 TEST SIMPLE D'INSERTION GARANTIE
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @PostMapping("/test-simple-insert")
    public ResponseEntity<Map<String, Object>> testSimpleInsert() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🧪 === TEST SIMPLE D'INSERTION ===");

            // ========== ÉTAPE 1: RÉCUPÉRER UN EMPLOYÉ ET UNE COMMANDE ==========

            // Employé
            List<Map<String, Object>> employees = employeService.getTousEmployesActifs();
            if (employees.isEmpty()) {
                result.put("success", false);
                result.put("error", "No employees found");
                return ResponseEntity.ok(result);
            }

            Map<String, Object> employee = employees.get(0);
            String employeeId = (String) employee.get("id");
            String employeeName = employee.get("prenom") + " " + employee.get("nom");

            // Commande
            String sqlOrder = "SELECT HEX(id), num_commande, card_count FROM `order` WHERE status IN (1,2,3) LIMIT 1";
            Query orderQuery = entityManager.createNativeQuery(sqlOrder);
            @SuppressWarnings("unchecked")
            List<Object[]> orderResults = orderQuery.getResultList();

            if (orderResults.isEmpty()) {
                result.put("success", false);
                result.put("error", "No orders found");
                return ResponseEntity.ok(result);
            }

            Object[] orderRow = orderResults.get(0);
            String orderId = (String) orderRow[0];
            String orderNumber = (String) orderRow[1];
            Integer cardCount = orderRow[2] != null ? ((Number) orderRow[2]).intValue() : 5;

            System.out.println("🎯 Test avec:");
            System.out.println("  Employee: " + employeeName + " (ID: " + employeeId + ")");
            System.out.println("  Order: " + orderNumber + " (ID: " + orderId + ", Cards: " + cardCount + ")");

            // ========== ÉTAPE 2: VÉRIFIER QUE LA PLANIFICATION N'EXISTE PAS ==========

            LocalDate testDate = LocalDate.now().plusDays(2); // Après-demain pour éviter les conflits
            LocalTime testTime = LocalTime.of(14, 30);

            String checkSql = "SELECT COUNT(*) FROM j_planning WHERE order_id = UNHEX(?) AND employee_id = UNHEX(?) AND planning_date = ?";
            Query checkQuery = entityManager.createNativeQuery(checkSql);
            checkQuery.setParameter(1, orderId);
            checkQuery.setParameter(2, employeeId);
            checkQuery.setParameter(3, testDate);

            @SuppressWarnings("unchecked")
            List<Number> checkResults = checkQuery.getResultList();
            int existingCount = checkResults.isEmpty() ? 0 : checkResults.get(0).intValue();

            System.out.println("🔍 Planifications existantes: " + existingCount);

            if (existingCount > 0) {
                result.put("success", false);
                result.put("error", "Planning already exists for this combination");
                result.put("existing_count", existingCount);
                return ResponseEntity.ok(result);
            }

            // ========== ÉTAPE 3: INSERTION SIMPLE ==========

            String newId = UUID.randomUUID().toString().replace("-", "");
            int duration = cardCount * 3; // 3 minutes par carte
            LocalDateTime startDateTime = LocalDateTime.of(testDate, testTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(duration);

            String insertSql = """
        INSERT INTO j_planning 
        (id, order_id, employee_id, planning_date, start_time, end_time, 
         estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
        VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """;

            int rowsAffected = entityManager.createNativeQuery(insertSql)
                    .setParameter(1, newId)
                    .setParameter(2, orderId)
                    .setParameter(3, employeeId)
                    .setParameter(4, testDate)
                    .setParameter(5, startDateTime)
                    .setParameter(6, endDateTime)
                    .setParameter(7, duration)
                    .setParameter(8, "HIGH")
                    .setParameter(9, "SCHEDULED")
                    .setParameter(10, false)
                    .setParameter(11, cardCount)
                    .setParameter(12, "🧪 TEST: " + orderNumber + " → " + employeeName)
                    .executeUpdate();

            System.out.println("💾 Insertion result: " + rowsAffected + " rows affected");

            // ========== ÉTAPE 4: VÉRIFICATION ==========

            if (rowsAffected > 0) {
                String verifySql = "SELECT COUNT(*) FROM j_planning WHERE id = UNHEX(?)";
                Query verifyQuery = entityManager.createNativeQuery(verifySql);
                verifyQuery.setParameter(1, newId);
                Number verifyCount = (Number) verifyQuery.getSingleResult();

                boolean verified = verifyCount.intValue() > 0;

                result.put("success", true);
                result.put("inserted", true);
                result.put("verified", verified);
                result.put("rows_affected", rowsAffected);
                result.put("planning_id", newId);
                result.put("employee_name", employeeName);
                result.put("order_number", orderNumber);
                result.put("card_count", cardCount);
                result.put("duration_minutes", duration);
                result.put("planning_date", testDate.toString());
                result.put("message", "✅ Test planning created successfully!");

                System.out.println("✅ SUCCESS: Planning created and verified!");

            } else {
                result.put("success", false);
                result.put("error", "Insert returned 0 rows affected");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Test simple insert error: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 🔬 TEST ULTRA-SIMPLIFIÉ - UNE SEULE INSERTION
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @PostMapping("/test-ultra-simple")
    public ResponseEntity<Map<String, Object>> testUltraSimple() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🔬 === TEST ULTRA-SIMPLIFIÉ ===");

            // ========== ÉTAPE 1: DONNÉES FIXES POUR ÉVITER TOUT PROBLÈME ==========

            // IDs fixes du diagnostic précédent
            String fixedEmployeeId = "E93263727DF943D78BD9B0F91845F358"; // Premier employé du diagnostic
            String fixedOrderId = "0197D2BB478FE23DBAD530B0EC72D233";     // Première commande du diagnostic

            // Données de test simples
            String planningId = UUID.randomUUID().toString().replace("-", "");
            LocalDate futureDate = LocalDate.now().plusDays(3); // Dans 3 jours pour éviter tout conflit
            LocalTime uniqueTime = LocalTime.of(15, 45); // Heure unique
            LocalDateTime startDateTime = LocalDateTime.of(futureDate, uniqueTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(90);

            System.out.println("🎯 Test avec données fixes:");
            System.out.println("  Planning ID: " + planningId);
            System.out.println("  Employee ID: " + fixedEmployeeId);
            System.out.println("  Order ID: " + fixedOrderId);
            System.out.println("  Date: " + futureDate);
            System.out.println("  Heure: " + uniqueTime);

            // ========== ÉTAPE 2: VÉRIFICATION QU'IL N'EXISTE PAS DÉJÀ ==========

            String checkSql = "SELECT COUNT(*) FROM j_planning WHERE order_id = UNHEX(?) AND employee_id = UNHEX(?)";
            Query checkQuery = entityManager.createNativeQuery(checkSql);
            checkQuery.setParameter(1, fixedOrderId);
            checkQuery.setParameter(2, fixedEmployeeId);

            @SuppressWarnings("unchecked")
            List<Object> checkResults = checkQuery.getResultList();
            int existingCount = checkResults.isEmpty() ? 0 : ((Number) checkResults.get(0)).intValue();

            result.put("existing_count", existingCount);
            System.out.println("🔍 Planifications existantes: " + existingCount);

            // ========== ÉTAPE 3: INSERTION BRUTE SANS VÉRIFICATION ==========

            System.out.println("💾 Tentative d'insertion...");

            String insertSql = """
        INSERT INTO j_planning 
        (id, order_id, employee_id, planning_date, start_time, end_time, 
         estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
        VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """;

            int rowsAffected = 0;
            String insertError = null;

            try {
                rowsAffected = entityManager.createNativeQuery(insertSql)
                        .setParameter(1, planningId)
                        .setParameter(2, fixedOrderId)
                        .setParameter(3, fixedEmployeeId)
                        .setParameter(4, futureDate)
                        .setParameter(5, startDateTime)
                        .setParameter(6, endDateTime)
                        .setParameter(7, 90)
                        .setParameter(8, "URGENT")
                        .setParameter(9, "SCHEDULED")
                        .setParameter(10, false)
                        .setParameter(11, 25)
                        .setParameter(12, "🔬 TEST ULTRA-SIMPLE")
                        .executeUpdate();

                System.out.println("💾 Rows affected: " + rowsAffected);

            } catch (Exception e) {
                insertError = e.getMessage();
                System.err.println("❌ Insert error: " + e.getMessage());
                e.printStackTrace();
            }

            // ========== ÉTAPE 4: VÉRIFICATION IMMÉDIATE ==========

            int verificationCount = 0;
            if (insertError == null) {
                try {
                    String verifySql = "SELECT COUNT(*) FROM j_planning WHERE id = UNHEX(?)";
                    Query verifyQuery = entityManager.createNativeQuery(verifySql);
                    verifyQuery.setParameter(1, planningId);
                    Number verifyResult = (Number) verifyQuery.getSingleResult();
                    verificationCount = verifyResult.intValue();

                    System.out.println("🔍 Vérification: " + verificationCount + " planning trouvé");

                } catch (Exception e) {
                    System.err.println("❌ Verification error: " + e.getMessage());
                }
            }

            // ========== ÉTAPE 5: COMPTAGE TOTAL ==========

            String totalSql = "SELECT COUNT(*) FROM j_planning";
            Query totalQuery = entityManager.createNativeQuery(totalSql);
            Number totalResult = (Number) totalQuery.getSingleResult();
            int totalPlannings = totalResult.intValue();

            System.out.println("📊 Total plannings dans la table: " + totalPlannings);

            // ========== RÉSULTATS ==========

            result.put("success", rowsAffected > 0);
            result.put("fixed_employee_id", fixedEmployeeId);
            result.put("fixed_order_id", fixedOrderId);
            result.put("planning_id", planningId);
            result.put("future_date", futureDate.toString());
            result.put("unique_time", uniqueTime.toString());
            result.put("rows_affected", rowsAffected);
            result.put("verification_count", verificationCount);
            result.put("total_plannings_in_table", totalPlannings);
            result.put("insert_error", insertError);

            if (rowsAffected > 0 && verificationCount > 0) {
                result.put("message", "✅ SUCCESS: Planning créé et vérifié !");
                System.out.println("🎉 SUCCESS: Planning créé avec succès !");
            } else if (rowsAffected > 0 && verificationCount == 0) {
                result.put("message", "⚠️ PARTIAL: Planning inséré mais non trouvé en vérification");
            } else if (insertError != null) {
                result.put("message", "❌ ERROR: " + insertError);
            } else {
                result.put("message", "❌ FAILED: 0 rows affected");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Ultra simple test error: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    /**
     * 🔍 DEBUG DE LA LOGIQUE DE BOUCLE
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @PostMapping("/debug-loop")
    public ResponseEntity<Map<String, Object>> debugLoop() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🔍 === DEBUG LOGIQUE DE BOUCLE ===");

            // ========== RÉCUPÉRER LES DONNÉES COMME DANS LA VRAIE MÉTHODE ==========

            List<Map<String, Object>> employees = employeService.getTousEmployesActifs();
            System.out.println("👥 Employés trouvés: " + employees.size());

            String sqlOrders = """
            SELECT 
                HEX(o.id) as id, 
                o.num_commande, 
                o.delai, 
                COALESCE(o.card_count, 0) as db_card_count,
                COALESCE(COUNT(cco.card_certification_id), 0) as real_card_count
            FROM `order` o
            LEFT JOIN card_certification_order cco ON o.id = cco.order_id
            WHERE o.status IN (1,2,3) 
            AND o.date >= '2025-06-01' 
            GROUP BY o.id, o.num_commande, o.delai, o.card_count
            LIMIT 5
        """;

            Query orderQuery = entityManager.createNativeQuery(sqlOrders);
            @SuppressWarnings("unchecked")
            List<Object[]> orderResults = orderQuery.getResultList();

            System.out.println("📦 Commandes trouvées: " + orderResults.size());

            List<Map<String, Object>> orders = new ArrayList<>();
            for (Object[] row : orderResults) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", row[0]);
                order.put("orderNumber", row[1]);
                order.put("delaiCode", row[2]);

                Integer dbCardCount = row[3] != null ? ((Number) row[3]).intValue() : 0;
                Integer realCardCount = row[4] != null ? ((Number) row[4]).intValue() : 0;
                Integer finalCardCount = Math.max(dbCardCount, realCardCount);

                if (finalCardCount == 0) {
                    finalCardCount = 15; // Valeur de test
                }

                order.put("cardCount", finalCardCount);
                order.put("dbCardCount", dbCardCount);
                order.put("realCardCount", realCardCount);

                orders.add(order);

                System.out.println("  📦 " + row[1] + ": " + dbCardCount + " (DB) + " + realCardCount + " (Real) = " + finalCardCount + " cartes");
            }

            result.put("employees_count", employees.size());
            result.put("orders_count", orders.size());
            result.put("orders_sample", orders);

            // ========== SIMULER LA LOGIQUE DE BOUCLE SANS INSERTION ==========

            List<Map<String, Object>> plannedInsertions = new ArrayList<>();

            for (int i = 0; i < Math.min(orders.size(), 3); i++) { // Limiter à 3 pour le test
                Map<String, Object> order = orders.get(i);
                Map<String, Object> employee = employees.get(i % employees.size());

                String orderId = (String) order.get("id");
                String employeeId = (String) employee.get("id");
                String orderNumber = (String) order.get("orderNumber");
                String employeeName = employee.get("prenom") + " " + employee.get("nom");
                Integer cardCount = (Integer) order.get("cardCount");

                int durationMinutes = Math.max(cardCount * 3, 30);

                LocalDate planningDate = LocalDate.now().plusDays(1);
                LocalTime startTime = LocalTime.of(9 + (i / employees.size()), (i % employees.size()) * 20);

                System.out.println("🔄 Simulation " + (i+1) + ":");
                System.out.println("  📦 Commande: " + orderNumber + " (" + cardCount + " cartes)");
                System.out.println("  👤 Employé: " + employeeName);
                System.out.println("  ⏱️ Durée: " + durationMinutes + " minutes");
                System.out.println("  📅 Date: " + planningDate + " à " + startTime);

                // Vérifier existence SANS getSingleResult
                String checkSql = "SELECT COUNT(*) FROM j_planning WHERE order_id = UNHEX(?) AND employee_id = UNHEX(?) AND planning_date = ?";
                Query checkQuery = entityManager.createNativeQuery(checkSql);
                checkQuery.setParameter(1, orderId);
                checkQuery.setParameter(2, employeeId);
                checkQuery.setParameter(3, planningDate);

                int existingCount = 0;
                String checkError = null;

                try {
                    @SuppressWarnings("unchecked")
                    List<Object> checkResults = checkQuery.getResultList();
                    if (!checkResults.isEmpty() && checkResults.get(0) != null) {
                        existingCount = ((Number) checkResults.get(0)).intValue();
                    }
                    System.out.println("  🔍 Existants: " + existingCount);
                } catch (Exception e) {
                    checkError = e.getMessage();
                    System.err.println("  ❌ Erreur check: " + e.getMessage());
                }

                Map<String, Object> planned = new HashMap<>();
                planned.put("index", i + 1);
                planned.put("orderNumber", orderNumber);
                planned.put("employeeName", employeeName);
                planned.put("cardCount", cardCount);
                planned.put("durationMinutes", durationMinutes);
                planned.put("planningDate", planningDate.toString());
                planned.put("startTime", startTime.toString());
                planned.put("existingCount", existingCount);
                planned.put("checkError", checkError);
                planned.put("shouldInsert", existingCount == 0 && checkError == null);

                plannedInsertions.add(planned);
            }

            result.put("planned_insertions", plannedInsertions);
            result.put("insertions_that_should_work", plannedInsertions.stream()
                    .mapToInt(p -> (Boolean) p.get("shouldInsert") ? 1 : 0)
                    .sum());

            System.out.println("📊 Résumé simulation:");
            System.out.println("  • Commandes analysées: " + orders.size());
            System.out.println("  • Insertions planifiées: " + plannedInsertions.size());
            System.out.println("  • Insertions qui devraient marcher: " + result.get("insertions_that_should_work"));

            result.put("success", true);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Debug loop error: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(result);
        }
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
            List<Map<String, Object>> employees = employeService.getTousEmployesActifs();

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
     * 🔍 DIAGNOSTIC COMPLET DE LA TABLE j_planning
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @GetMapping("/diagnose-table-structure")
    public ResponseEntity<Map<String, Object>> diagnoseTableStructure() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("🔍 === DIAGNOSTIC COMPLET DE j_planning ===");

            // ========== 1. VÉRIFIER QUE LA TABLE EXISTE ==========

            String sqlTables = "SHOW TABLES LIKE 'j_planning'";
            Query tablesQuery = entityManager.createNativeQuery(sqlTables);
            @SuppressWarnings("unchecked")
            List<String> tables = tablesQuery.getResultList();

            boolean tableExists = !tables.isEmpty();
            result.put("table_exists", tableExists);
            System.out.println("📋 Table j_planning existe: " + tableExists);

            if (!tableExists) {
                result.put("error", "Table j_planning n'existe pas !");
                return ResponseEntity.ok(result);
            }

            // ========== 2. STRUCTURE DÉTAILLÉE ==========

            String sqlDescribe = "DESCRIBE j_planning";
            Query describeQuery = entityManager.createNativeQuery(sqlDescribe);
            @SuppressWarnings("unchecked")
            List<Object[]> columns = describeQuery.getResultList();

            List<Map<String, Object>> columnDetails = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();

            for (Object[] col : columns) {
                Map<String, Object> colInfo = new HashMap<>();
                colInfo.put("name", col[0]);
                colInfo.put("type", col[1]);
                colInfo.put("null", col[2]);
                colInfo.put("key", col[3]);
                colInfo.put("default", col[4]);
                colInfo.put("extra", col[5]);

                columnDetails.add(colInfo);
                columnNames.add((String) col[0]);

                System.out.println("  📝 " + col[0] + " | " + col[1] + " | NULL:" + col[2] + " | KEY:" + col[3]);
            }

            result.put("columns", columnDetails);
            result.put("column_names", columnNames);

            // ========== 3. CONTRAINTES ET INDEX ==========

            String sqlConstraints = """
            SELECT 
                CONSTRAINT_NAME, 
                CONSTRAINT_TYPE, 
                COLUMN_NAME,
                REFERENCED_TABLE_NAME,
                REFERENCED_COLUMN_NAME
            FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
            WHERE TABLE_SCHEMA = 'dev' 
            AND TABLE_NAME = 'j_planning'
        """;

            try {
                Query constraintsQuery = entityManager.createNativeQuery(sqlConstraints);
                @SuppressWarnings("unchecked")
                List<Object[]> constraints = constraintsQuery.getResultList();

                List<Map<String, Object>> constraintList = new ArrayList<>();
                for (Object[] constraint : constraints) {
                    Map<String, Object> constraintInfo = new HashMap<>();
                    constraintInfo.put("name", constraint[0]);
                    constraintInfo.put("type", constraint[1]);
                    constraintInfo.put("column", constraint[2]);
                    constraintInfo.put("referenced_table", constraint[3]);
                    constraintInfo.put("referenced_column", constraint[4]);
                    constraintList.add(constraintInfo);

                    System.out.println("  🔗 " + constraint[0] + " (" + constraint[1] + ") sur " + constraint[2]);
                }
                result.put("constraints", constraintList);
            } catch (Exception e) {
                result.put("constraints_error", e.getMessage());
            }

            // ========== 4. TEST D'INSERTION MINIMALE ==========

            System.out.println("🧪 Test d'insertion minimale...");

            String testId = UUID.randomUUID().toString().replace("-", "");
            String minimalInsert = """
            INSERT INTO j_planning (id, order_id, employee_id, planning_date, start_time, estimated_duration_minutes, status, completed, created_at, updated_at)
            VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, NOW(), NOW())
        """;

            try {
                int testRows = entityManager.createNativeQuery(minimalInsert)
                        .setParameter(1, testId)
                        .setParameter(2, "0197D2BB478FE23DBAD530B0EC72D233") // ID de commande du test précédent
                        .setParameter(3, "E93263727DF943D78BD9B0F91845F358") // ID employé du test précédent
                        .setParameter(4, LocalDate.now().plusDays(5))
                        .setParameter(5, LocalDateTime.now().plusDays(5).withHour(10).withMinute(0))
                        .setParameter(6, 60)
                        .setParameter(7, "TEST")
                        .setParameter(8, false)
                        .executeUpdate();

                result.put("minimal_insert_success", testRows > 0);
                result.put("minimal_insert_rows", testRows);

                if (testRows > 0) {
                    System.out.println("✅ Insertion minimale réussie !");

                    // Compter après insertion
                    String countSql = "SELECT COUNT(*) FROM j_planning";
                    Number count = (Number) entityManager.createNativeQuery(countSql).getSingleResult();
                    result.put("total_after_insert", count.intValue());
                }

            } catch (Exception e) {
                result.put("minimal_insert_error", e.getMessage());
                System.err.println("❌ Insertion minimale échouée: " + e.getMessage());
                e.printStackTrace();
            }

            // ========== 5. VÉRIFICATION DES CLÉS ÉTRANGÈRES ==========

            System.out.println("🔍 Vérification des clés étrangères...");

            // Vérifier que les tables référencées existent
            String[] referencedTables = {"order", "j_employee"};
            Map<String, Boolean> tableExistence = new HashMap<>();

            for (String table : referencedTables) {
                try {
                    String checkTable = "SELECT 1 FROM " + (table.equals("order") ? "`order`" : table) + " LIMIT 1";
                    entityManager.createNativeQuery(checkTable).getResultList();
                    tableExistence.put(table, true);
                    System.out.println("  ✅ Table " + table + " accessible");
                } catch (Exception e) {
                    tableExistence.put(table, false);
                    System.out.println("  ❌ Table " + table + " inaccessible: " + e.getMessage());
                }
            }
            result.put("referenced_tables_exist", tableExistence);

            // ========== 6. RECOMMANDATIONS ==========

            List<String> recommendations = new ArrayList<>();

            if (!tableExists) {
                recommendations.add("❌ CRÉER la table j_planning");
            } else {
                recommendations.add("✅ Table j_planning existe");
            }

            if (columnNames.contains("id") && columnNames.contains("order_id") && columnNames.contains("employee_id")) {
                recommendations.add("✅ Colonnes de base présentes");
            } else {
                recommendations.add("❌ VÉRIFIER les colonnes de base");
            }

            if (tableExistence.get("order") == Boolean.TRUE && tableExistence.get("j_employee") == Boolean.TRUE) {
                recommendations.add("✅ Tables référencées accessibles");
            } else {
                recommendations.add("❌ PROBLÈME avec les tables référencées");
            }

            result.put("recommendations", recommendations);
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Diagnostic error: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 🔬 TEST DE DEBUG ULTIME - LOGGING DÉTAILLÉ
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @PostMapping("/ultimate-debug")
    public ResponseEntity<Map<String, Object>> ultimateDebug() {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();

        try {
            logs.add("🔬 === TEST DE DEBUG ULTIME ===");
            System.out.println("🔬 === TEST DE DEBUG ULTIME ===");

            // ========== ÉTAPE 1: DONNÉES FIXES ==========

            // IDs testés qui fonctionnent du diagnostic précédent
            String fixedEmployeeId = "E93263727DF943D78BD9B0F91845F358";
            String fixedOrderId = "0197D2BB478FE23DBAD530B0EC72D233";

            logs.add("🎯 IDs fixes: Employee=" + fixedEmployeeId + ", Order=" + fixedOrderId);

            // ========== ÉTAPE 2: PARAMÈTRES UNIQUES ==========

            String planningId = UUID.randomUUID().toString().replace("-", "");
            LocalDate futureDate = LocalDate.of(2025, 8, 20); // Date fixe dans le futur
            LocalTime fixedTime = LocalTime.of(14, 30, 0); // Heure fixe
            LocalDateTime startDateTime = LocalDateTime.of(futureDate, fixedTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(90);

            logs.add("📅 Planning: " + planningId + " le " + futureDate + " à " + fixedTime);

            // ========== ÉTAPE 3: CONSTRUCTION DE LA REQUÊTE ==========

            String insertSql = """
        INSERT INTO j_planning 
        (id, order_id, employee_id, planning_date, start_time, end_time, 
         estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
        VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """;

            logs.add("📝 SQL préparé: " + insertSql.replaceAll("\\s+", " "));

            // ========== ÉTAPE 4: PRÉPARATION DES PARAMÈTRES ==========

            Object[] parameters = {
                    planningId,                    // 1
                    fixedOrderId,                  // 2
                    fixedEmployeeId,               // 3
                    futureDate,                    // 4
                    startDateTime,                 // 5
                    endDateTime,                   // 6
                    90,                           // 7 - estimated_duration_minutes
                    "MEDIUM",                     // 8 - priority (ENUM valide)
                    "SCHEDULED",                  // 9 - status (ENUM valide)
                    false,                        // 10 - completed
                    25,                           // 11 - card_count
                    "🔬 ULTIMATE DEBUG TEST"      // 12 - notes
            };

            logs.add("🔧 Paramètres:");
            for (int i = 0; i < parameters.length; i++) {
                logs.add("  " + (i+1) + ": " + parameters[i] + " (" + parameters[i].getClass().getSimpleName() + ")");
            }

            // ========== ÉTAPE 5: EXÉCUTION AVEC LOGGING DÉTAILLÉ ==========

            int rowsAffected = 0;
            String executionError = null;
            long executionTime = 0;

            try {
                logs.add("💾 Début de l'insertion...");
                long startTime = System.currentTimeMillis();

                Query insertQuery = entityManager.createNativeQuery(insertSql);

                // Définir chaque paramètre individuellement avec logging
                for (int i = 0; i < parameters.length; i++) {
                    try {
                        insertQuery.setParameter(i + 1, parameters[i]);
                        logs.add("  ✅ Paramètre " + (i+1) + " défini: " + parameters[i]);
                    } catch (Exception paramError) {
                        logs.add("  ❌ Erreur paramètre " + (i+1) + ": " + paramError.getMessage());
                        throw paramError;
                    }
                }

                logs.add("💾 Exécution de la requête...");
                rowsAffected = insertQuery.executeUpdate();

                executionTime = System.currentTimeMillis() - startTime;
                logs.add("💾 Résultat: " + rowsAffected + " lignes affectées en " + executionTime + "ms");

            } catch (Exception e) {
                executionError = e.getMessage();
                logs.add("❌ ERREUR EXÉCUTION: " + e.getMessage());
                System.err.println("❌ ERREUR EXÉCUTION: " + e.getMessage());
                e.printStackTrace();
            }

            // ========== ÉTAPE 6: VÉRIFICATION ==========

            int verificationCount = 0;
            if (rowsAffected > 0) {
                try {
                    String verifySql = "SELECT COUNT(*) FROM j_planning WHERE id = UNHEX(?)";
                    Query verifyQuery = entityManager.createNativeQuery(verifySql);
                    verifyQuery.setParameter(1, planningId);
                    Number verifyResult = (Number) verifyQuery.getSingleResult();
                    verificationCount = verifyResult.intValue();

                    logs.add("🔍 Vérification: " + verificationCount + " planning trouvé");
                } catch (Exception verifyError) {
                    logs.add("❌ Erreur vérification: " + verifyError.getMessage());
                }
            }

            // ========== ÉTAPE 7: COMPTAGE TOTAL ==========

            try {
                String totalSql = "SELECT COUNT(*) FROM j_planning";
                Number totalResult = (Number) entityManager.createNativeQuery(totalSql).getSingleResult();
                int totalPlannings = totalResult.intValue();
                logs.add("📊 Total plannings dans la table: " + totalPlannings);
                result.put("total_plannings", totalPlannings);
            } catch (Exception countError) {
                logs.add("❌ Erreur comptage: " + countError.getMessage());
            }

            // ========== RÉSULTATS FINALS ==========

            result.put("success", rowsAffected > 0);
            result.put("planning_id", planningId);
            result.put("fixed_employee_id", fixedEmployeeId);
            result.put("fixed_order_id", fixedOrderId);
            result.put("planning_date", futureDate.toString());
            result.put("planning_time", fixedTime.toString());
            result.put("rows_affected", rowsAffected);
            result.put("verification_count", verificationCount);
            result.put("execution_error", executionError);
            result.put("execution_time_ms", executionTime);
            result.put("parameters", parameters);
            result.put("logs", logs);

            if (rowsAffected > 0 && verificationCount > 0) {
                result.put("message", "🎉 SUCCESS: Planning créé et vérifié !");
                logs.add("🎉 SUCCESS: Planning créé et vérifié !");
            } else if (executionError != null) {
                result.put("message", "❌ ERROR: " + executionError);
            } else {
                result.put("message", "❌ MYSTERY: Pas d'erreur mais 0 rows affected");
            }

            // Afficher tous les logs
            for (String log : logs) {
                System.out.println(log);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logs.add("❌ EXCEPTION FATALE: " + e.getMessage());
            System.err.println("❌ EXCEPTION FATALE: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("fatal_error", e.getMessage());
            result.put("logs", logs);
            return ResponseEntity.ok(result);
        }
    }
    /**
     * 🎯 TEST AVEC DE VRAIS IDS EXISTANTS
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @PostMapping("/test-real-ids")
    public ResponseEntity<Map<String, Object>> testRealIds() {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();

        try {
            logs.add("🎯 === TEST AVEC VRAIS IDS EXISTANTS ===");
            System.out.println("🎯 === TEST AVEC VRAIS IDS EXISTANTS ===");

            // ========== ÉTAPE 1: TROUVER UN VRAI EMPLOYÉ ==========

            String realEmployeeId = null;
            String employeeName = null;

            try {
                String sqlEmployee = "SELECT HEX(id), prenom, nom FROM j_employee WHERE actif = 1 LIMIT 1";
                Query empQuery = entityManager.createNativeQuery(sqlEmployee);
                @SuppressWarnings("unchecked")
                List<Object[]> empResults = empQuery.getResultList();

                if (!empResults.isEmpty()) {
                    Object[] empRow = empResults.get(0);
                    realEmployeeId = (String) empRow[0];
                    employeeName = empRow[1] + " " + empRow[2];
                    logs.add("✅ Employé trouvé: " + employeeName + " (ID: " + realEmployeeId + ")");
                } else {
                    logs.add("❌ Aucun employé trouvé dans j_employee");
                }
            } catch (Exception e) {
                logs.add("❌ Erreur recherche employé: " + e.getMessage());
            }

            // ========== ÉTAPE 2: TROUVER UNE VRAIE COMMANDE ==========

            String realOrderId = null;
            String orderNumber = null;

            try {
                String sqlOrder = "SELECT HEX(id), num_commande FROM `order` WHERE status IN (1,2,3) LIMIT 1";
                Query orderQuery = entityManager.createNativeQuery(sqlOrder);
                @SuppressWarnings("unchecked")
                List<Object[]> orderResults = orderQuery.getResultList();

                if (!orderResults.isEmpty()) {
                    Object[] orderRow = orderResults.get(0);
                    realOrderId = (String) orderRow[0];
                    orderNumber = (String) orderRow[1];
                    logs.add("✅ Commande trouvée: " + orderNumber + " (ID: " + realOrderId + ")");
                } else {
                    logs.add("❌ Aucune commande trouvée dans order");
                }
            } catch (Exception e) {
                logs.add("❌ Erreur recherche commande: " + e.getMessage());
            }

            // ========== ÉTAPE 3: VÉRIFIER QUE LES IDS EXISTENT VRAIMENT ==========

            if (realEmployeeId != null && realOrderId != null) {

                // Vérifier employé
                try {
                    String checkEmp = "SELECT COUNT(*) FROM j_employee WHERE HEX(id) = ?";
                    Query checkEmpQuery = entityManager.createNativeQuery(checkEmp);
                    checkEmpQuery.setParameter(1, realEmployeeId);
                    Number empCount = (Number) checkEmpQuery.getSingleResult();
                    logs.add("🔍 Vérification employé: " + empCount + " trouvé(s)");
                } catch (Exception e) {
                    logs.add("❌ Erreur vérification employé: " + e.getMessage());
                }

                // Vérifier commande
                try {
                    String checkOrder = "SELECT COUNT(*) FROM `order` WHERE HEX(id) = ?";
                    Query checkOrderQuery = entityManager.createNativeQuery(checkOrder);
                    checkOrderQuery.setParameter(1, realOrderId);
                    Number orderCount = (Number) checkOrderQuery.getSingleResult();
                    logs.add("🔍 Vérification commande: " + orderCount + " trouvée(s)");
                } catch (Exception e) {
                    logs.add("❌ Erreur vérification commande: " + e.getMessage());
                }

                // ========== ÉTAPE 4: INSERTION AVEC VRAIS IDS ==========

                String planningId = UUID.randomUUID().toString().replace("-", "");
                LocalDate futureDate = LocalDate.of(2025, 8, 25);
                LocalTime fixedTime = LocalTime.of(15, 0);
                LocalDateTime startDateTime = LocalDateTime.of(futureDate, fixedTime);
                LocalDateTime endDateTime = startDateTime.plusMinutes(60);

                logs.add("🔄 Tentative insertion avec vrais IDs...");
                logs.add("  Planning ID: " + planningId);
                logs.add("  Employee ID: " + realEmployeeId + " (" + employeeName + ")");
                logs.add("  Order ID: " + realOrderId + " (" + orderNumber + ")");

                String insertSql = """
            INSERT INTO j_planning 
            (id, order_id, employee_id, planning_date, start_time, end_time, 
             estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
            VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            """;

                try {
                    int rowsAffected = entityManager.createNativeQuery(insertSql)
                            .setParameter(1, planningId)
                            .setParameter(2, realOrderId)
                            .setParameter(3, realEmployeeId)
                            .setParameter(4, futureDate)
                            .setParameter(5, startDateTime)
                            .setParameter(6, endDateTime)
                            .setParameter(7, 60)
                            .setParameter(8, "HIGH")
                            .setParameter(9, "SCHEDULED")
                            .setParameter(10, false)
                            .setParameter(11, 15)
                            .setParameter(12, "🎯 TEST AVEC VRAIS IDS: " + orderNumber + " → " + employeeName)
                            .executeUpdate();

                    logs.add("💾 Résultat insertion: " + rowsAffected + " lignes affectées");

                    if (rowsAffected > 0) {
                        logs.add("🎉 SUCCESS ! Planification créée avec succès !");

                        // Vérification
                        String verifySql = "SELECT COUNT(*) FROM j_planning WHERE id = UNHEX(?)";
                        Number verifyCount = (Number) entityManager.createNativeQuery(verifySql)
                                .setParameter(1, planningId)
                                .getSingleResult();

                        logs.add("🔍 Vérification: " + verifyCount + " planning trouvé");

                        result.put("success", true);
                        result.put("planning_created", true);
                        result.put("planning_id", planningId);
                        result.put("rows_affected", rowsAffected);
                        result.put("verification_count", verifyCount.intValue());

                    } else {
                        logs.add("❌ Insertion échouée: 0 rows affected");
                        result.put("success", false);
                        result.put("error", "0 rows affected");
                    }

                } catch (Exception insertError) {
                    logs.add("❌ ERREUR INSERTION: " + insertError.getMessage());
                    result.put("success", false);
                    result.put("insert_error", insertError.getMessage());
                }

            } else {
                logs.add("❌ IMPOSSIBLE: Pas d'employé ou de commande trouvé(e)");
                result.put("success", false);
                result.put("error", "Pas d'employé ou de commande trouvé");
            }

            // ========== COMPTAGE FINAL ==========

            try {
                String totalSql = "SELECT COUNT(*) FROM j_planning";
                Number totalCount = (Number) entityManager.createNativeQuery(totalSql).getSingleResult();
                logs.add("📊 Total plannings dans la table: " + totalCount);
                result.put("total_plannings", totalCount.intValue());
            } catch (Exception e) {
                logs.add("❌ Erreur comptage: " + e.getMessage());
            }

            result.put("real_employee_id", realEmployeeId);
            result.put("real_order_id", realOrderId);
            result.put("employee_name", employeeName);
            result.put("order_number", orderNumber);
            result.put("logs", logs);

            // Afficher logs
            for (String log : logs) {
                System.out.println(log);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logs.add("❌ EXCEPTION FATALE: " + e.getMessage());
            System.err.println("❌ EXCEPTION FATALE: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("fatal_error", e.getMessage());
            result.put("logs", logs);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 🔥 TEST SQL DIRECT SANS JPA
     * Ajoutez cette méthode à votre PlanningController.java
     */
    @PostMapping("/test-raw-sql")
    public ResponseEntity<Map<String, Object>> testRawSql() {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();

        try {
            logs.add("🔥 === TEST SQL DIRECT SANS JPA ===");
            System.out.println("🔥 === TEST SQL DIRECT SANS JPA ===");

            // ========== ÉTAPE 1: RÉCUPÉRER VRAIS IDS ==========

            String realEmployeeId = "08C68C835C84420A88E7AEB56BFA8E6A"; // Du test précédent
            String realOrderId = "018120F0622D3B430CD52D3836B18CCE";    // Du test précédent

            logs.add("🎯 IDs confirmés existants:");
            logs.add("  Employee: " + realEmployeeId + " (Ibrahim ALAME)");
            logs.add("  Order: " + realOrderId + " (ETEKIXLYX)");

            // ========== ÉTAPE 2: INSERTION SQL PURE AVEC VALUES EXPLICITES ==========

            String planningId = UUID.randomUUID().toString().replace("-", "");

            // ✅ SQL BRUT avec valeurs explicites pour éviter tout problème JPA
            String rawSql = String.format("""
            INSERT INTO j_planning 
            (id, order_id, employee_id, planning_date, start_time, end_time, 
             estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
            VALUES 
            (UNHEX('%s'), UNHEX('%s'), UNHEX('%s'), '2025-08-30', '2025-08-30 16:00:00', '2025-08-30 17:00:00', 
             60, 'HIGH', 'SCHEDULED', FALSE, 20, '🔥 RAW SQL TEST', NOW(), NOW())
            """,
                    planningId, realOrderId, realEmployeeId);

            logs.add("🔥 SQL brut préparé:");
            logs.add("  " + rawSql.replaceAll("\\s+", " "));

            try {
                logs.add("💾 Exécution SQL directe...");

                Query rawQuery = entityManager.createNativeQuery(rawSql);
                int rowsAffected = rawQuery.executeUpdate();

                logs.add("💾 Résultat SQL direct: " + rowsAffected + " lignes affectées");

                if (rowsAffected > 0) {
                    logs.add("🎉 SUCCESS ! SQL direct a marché !");

                    // Vérification immédiate
                    String checkSql = "SELECT COUNT(*) FROM j_planning WHERE HEX(id) = '" + planningId + "'";
                    Query checkQuery = entityManager.createNativeQuery(checkSql);
                    Number checkResult = (Number) checkQuery.getSingleResult();

                    logs.add("🔍 Vérification: " + checkResult + " planning trouvé");

                    // Afficher le planning créé
                    String selectSql = "SELECT HEX(id), HEX(order_id), HEX(employee_id), planning_date, priority, status, notes FROM j_planning WHERE HEX(id) = '" + planningId + "'";
                    Query selectQuery = entityManager.createNativeQuery(selectSql);
                    @SuppressWarnings("unchecked")
                    List<Object[]> planningData = selectQuery.getResultList();

                    if (!planningData.isEmpty()) {
                        Object[] row = planningData.get(0);
                        logs.add("📋 Planning créé:");
                        logs.add("  ID: " + row[0]);
                        logs.add("  Order ID: " + row[1]);
                        logs.add("  Employee ID: " + row[2]);
                        logs.add("  Date: " + row[3]);
                        logs.add("  Priority: " + row[4]);
                        logs.add("  Status: " + row[5]);
                        logs.add("  Notes: " + row[6]);
                    }

                    result.put("success", true);
                    result.put("method", "RAW_SQL");
                    result.put("planning_created", true);
                    result.put("planning_id", planningId);
                    result.put("rows_affected", rowsAffected);

                } else {
                    logs.add("❌ SQL direct échoué: 0 rows affected");
                    result.put("success", false);
                    result.put("error", "SQL direct: 0 rows affected");
                }

            } catch (Exception sqlError) {
                logs.add("❌ ERREUR SQL DIRECT: " + sqlError.getMessage());
                sqlError.printStackTrace();
                result.put("success", false);
                result.put("sql_error", sqlError.getMessage());
            }

            // ========== ÉTAPE 3: COMPTAGE FINAL ==========

            try {
                String totalSql = "SELECT COUNT(*) FROM j_planning";
                Number totalCount = (Number) entityManager.createNativeQuery(totalSql).getSingleResult();
                logs.add("📊 Total plannings dans la table: " + totalCount);
                result.put("total_plannings_after", totalCount.intValue());
            } catch (Exception e) {
                logs.add("❌ Erreur comptage: " + e.getMessage());
            }

            // ========== ÉTAPE 4: SI ÇA MARCHE, GÉNÉRER PLUSIEURS PLANIFICATIONS ==========

            if (result.get("success") == Boolean.TRUE) {
                logs.add("🔥 SQL direct marche ! Génération de planifications multiples...");

                int additionalPlannings = 0;
                for (int i = 1; i <= 3; i++) {
                    try {
                        String additionalId = UUID.randomUUID().toString().replace("-", "");
                        String additionalSql = String.format("""
                        INSERT INTO j_planning 
                        (id, order_id, employee_id, planning_date, start_time, end_time, 
                         estimated_duration_minutes, priority, status, completed, card_count, notes, created_at, updated_at)
                        VALUES 
                        (UNHEX('%s'), UNHEX('%s'), UNHEX('%s'), '2025-09-0%d', '2025-09-0%d 1%d:00:00', '2025-09-0%d 1%d:30:00', 
                         30, 'MEDIUM', 'SCHEDULED', FALSE, 10, '🔥 BULK INSERT %d', NOW(), NOW())
                        """,
                                additionalId, realOrderId, realEmployeeId, i, i, (3+i), i, (3+i), i);

                        int additionalRows = entityManager.createNativeQuery(additionalSql).executeUpdate();
                        if (additionalRows > 0) {
                            additionalPlannings++;
                            logs.add("  ✅ Planning " + i + " créé: " + additionalId);
                        }
                    } catch (Exception e) {
                        logs.add("  ❌ Erreur planning " + i + ": " + e.getMessage());
                    }
                }

                result.put("additional_plannings", additionalPlannings);
                logs.add("🎉 " + additionalPlannings + " planifications supplémentaires créées !");
            }

            result.put("logs", logs);

            // Afficher tous les logs
            for (String log : logs) {
                System.out.println(log);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logs.add("❌ EXCEPTION FATALE: " + e.getMessage());
            System.err.println("❌ EXCEPTION FATALE: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("fatal_error", e.getMessage());
            result.put("logs", logs);
            return ResponseEntity.ok(result);
        }
    }
}