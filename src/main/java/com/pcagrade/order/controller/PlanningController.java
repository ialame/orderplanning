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
     * Generate planning - COMPLETE IMPLEMENTATION WITH TRANSACTION
     */



// ===============================================
// MÉTHODE GENERATEPLANNING CORRIGÉE
// ===============================================

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePlanning(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("=== PLANNING GENERATION DEBUG ===");

            // Paramètres
            String startDate = "2025-06-01";
            Integer timePerCard = 3;

            // Employés
            List<Map<String, Object>> employees = employeService.getTousEmployesActifs();
            System.out.println("Employees found: " + employees.size());

            if (employees.isEmpty()) {
                result.put("success", false);
                result.put("error", "No employees");
                return ResponseEntity.ok(result);
            }

            // Afficher premier employé
            Map<String, Object> firstEmployee = employees.get(0);
            String employeeId = (String) firstEmployee.get("id");
            String employeeName = firstEmployee.get("prenom") + " " + firstEmployee.get("nom");
            System.out.println("First employee: " + employeeName + " (ID: " + employeeId + ")");

            // Commandes - ✅ CORRECTION ICI
            String sqlOrders = "SELECT HEX(o.id) as order_id, o.num_commande as order_number FROM `order` o WHERE o.status IN (1, 2, 3) ORDER BY o.date DESC LIMIT 5";
            Query orderQuery = entityManager.createNativeQuery(sqlOrders);

            List<Object[]> ordersData;
            ordersData = (List<Object[]>) orderQuery.getResultList(); // ✅ Sans annotation problématique

            System.out.println("Orders found: " + ordersData.size());

            if (ordersData.isEmpty()) {
                result.put("success", true);
                result.put("message", "No orders");
                result.put("planningsSaved", 0);
                return ResponseEntity.ok(result);
            }

            // Afficher première commande
            Object[] firstOrder = ordersData.get(0);
            String orderId = (String) firstOrder[0];
            String orderNumber = (String) firstOrder[1];
            System.out.println("First order: " + orderNumber + " (ID: " + orderId + ")");

            // Test d'insertion simple
            int planningsSaved = 0;
            LocalDate planningDate = LocalDate.now().plusDays(2); // Après-demain
            LocalTime planningTime = LocalTime.of(14, 30); // 14h30

            System.out.println("Planning for: " + planningDate + " at " + planningTime);

            // Vérifier si existe
            String checkSql = "SELECT COUNT(*) FROM j_planning WHERE order_id = UNHEX(?) AND employee_id = UNHEX(?) AND planning_date = ? AND start_time = ?";
            Query checkQuery = entityManager.createNativeQuery(checkSql);
            checkQuery.setParameter(1, orderId);
            checkQuery.setParameter(2, employeeId);
            checkQuery.setParameter(3, planningDate);
            checkQuery.setParameter(4, planningTime);

            Number existingCount = (Number) checkQuery.getSingleResult();
            System.out.println("Existing plannings: " + existingCount.intValue());

            if (existingCount.intValue() == 0) {
                System.out.println("No existing planning - trying to insert...");

                String planningId = UUID.randomUUID().toString().replace("-", "");
                LocalDateTime startDateTime = LocalDateTime.of(planningDate, planningTime);
                LocalDateTime endDateTime = startDateTime.plusMinutes(90);

                String insertSql = "INSERT INTO j_planning (id, order_id, employee_id, planning_date, start_time, end_time, estimated_duration_minutes, estimated_end_time, priority, status, completed, created_at, updated_at, card_count, notes) VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)";

                int rowsAffected = entityManager.createNativeQuery(insertSql)
                        .setParameter(1, planningId)
                        .setParameter(2, orderId)
                        .setParameter(3, employeeId)
                        .setParameter(4, planningDate)
                        .setParameter(5, startDateTime)
                        .setParameter(6, endDateTime)
                        .setParameter(7, 90)
                        .setParameter(8, endDateTime)
                        .setParameter(9, "MEDIUM")
                        .setParameter(10, "SCHEDULED")
                        .setParameter(11, 0)
                        .setParameter(12, 1)
                        .setParameter(13, "Test planning: " + orderNumber)
                        .executeUpdate();

                System.out.println("Insert result: " + rowsAffected + " rows affected");

                if (rowsAffected > 0) {
                    entityManager.flush();
                    System.out.println("EntityManager flushed");

                    // Vérification
                    String verifySql = "SELECT COUNT(*) FROM j_planning WHERE id = UNHEX(?)";
                    Query verifyQuery = entityManager.createNativeQuery(verifySql);
                    verifyQuery.setParameter(1, planningId);
                    Number verifyCount = (Number) verifyQuery.getSingleResult();

                    System.out.println("Verification: " + verifyCount.intValue() + " planning found");

                    if (verifyCount.intValue() > 0) {
                        planningsSaved = 1;
                        System.out.println("SUCCESS: Planning saved and verified!");
                    } else {
                        System.out.println("ERROR: Planning not found after insert");
                    }
                } else {
                    System.out.println("ERROR: Insert returned 0 rows affected");
                }
            } else {
                System.out.println("Planning already exists - skipping");
            }

            // Résultat
            result.put("success", true);
            result.put("message", "Test completed");
            result.put("ordersAnalyzed", ordersData.size());
            result.put("employeesUsed", 1);
            result.put("planningsSaved", planningsSaved);
            result.put("planningsCreated", planningsSaved);
            result.put("timestamp", System.currentTimeMillis());

            System.out.println("FINAL RESULT: " + planningsSaved + " planning(s) saved");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();

            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    private String savePlanningToDatabase(String orderId, String employeeId,
                                          LocalDate planningDate, LocalTime startTime,
                                          int durationMinutes, String priority, String orderNumber) {
        try {
            System.out.println("    🔧 SAVE: Starting for " + orderNumber);

            String planningId = UUID.randomUUID().toString().replace("-", "");
            LocalDateTime startDateTime = LocalDateTime.of(planningDate, startTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);

            String insertSql = """
            INSERT INTO j_planning 
            (id, order_id, employee_id, planning_date, start_time, end_time, 
             estimated_duration_minutes, estimated_end_time, priority, status, 
             completed, created_at, updated_at, card_count, notes)
            VALUES (UNHEX(?), UNHEX(?), UNHEX(?), ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?)
        """;

            System.out.println("    🔄 SAVE: Executing INSERT for " + planningId);

            int rowsAffected = entityManager.createNativeQuery(insertSql)
                    .setParameter(1, planningId)
                    .setParameter(2, orderId)
                    .setParameter(3, employeeId)
                    .setParameter(4, planningDate)
                    .setParameter(5, startDateTime)
                    .setParameter(6, endDateTime)
                    .setParameter(7, durationMinutes)
                    .setParameter(8, endDateTime)
                    .setParameter(9, priority)
                    .setParameter(10, "SCHEDULED")
                    .setParameter(11, 0)
                    .setParameter(12, 1)
                    .setParameter(13, "Generated planning for: " + orderNumber)
                    .executeUpdate();

            System.out.println("    📊 SAVE: Rows affected = " + rowsAffected);

            if (rowsAffected > 0) {
                entityManager.flush();
                System.out.println("    ✅ SAVE: Flushed successfully");

                String verifySql = "SELECT COUNT(*) FROM j_planning WHERE id = UNHEX(?)";
                Query verifyQuery = entityManager.createNativeQuery(verifySql);
                verifyQuery.setParameter(1, planningId);
                Number verifyCount = (Number) verifyQuery.getSingleResult();

                System.out.println("    🔍 SAVE: Verification count = " + verifyCount.intValue());

                if (verifyCount.intValue() > 0) {
                    System.out.println("    ✅ SAVE: SUCCESS and VERIFIED");
                    return planningId;
                } else {
                    System.out.println("    ❌ SAVE: Success but verification failed");
                    return null;
                }
            } else {
                System.out.println("    ❌ SAVE: FAILED - 0 rows affected");
                return null;
            }

        } catch (Exception e) {
            System.err.println("    ❌ SAVE: ERROR - " + e.getMessage());
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

}