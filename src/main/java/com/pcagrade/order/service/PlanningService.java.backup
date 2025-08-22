package com.pcagrade.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PlanningService {

    @Autowired
    private EntityManager entityManager;

    /**
     * 💾 SAUVEGARDE UNITAIRE avec transaction
     */
    @Transactional
    public boolean savePlanning(String orderId, String employeeId, LocalDate planningDate,
                                LocalDateTime startTime, int durationMinutes, String priority) {
        try {
            LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

            String sql = """
                INSERT INTO j_planning (
                    id, order_id, employee_id, planning_date, start_time, 
                    estimated_duration_minutes, estimated_end_time, 
                    priority, status, created_at, updated_at
                ) VALUES (
                    UUID_TO_BIN(UUID()), UNHEX(?), UNHEX(?), ?, ?, 
                    ?, ?, ?, 'SCHEDULED', NOW(), NOW()
                )
            """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, orderId.replace("-", ""));
            query.setParameter(2, employeeId.replace("-", ""));
            query.setParameter(3, planningDate);
            query.setParameter(4, startTime);
            query.setParameter(5, durationMinutes);
            query.setParameter(6, endTime);
            query.setParameter(7, priority);

            int result = query.executeUpdate();
            entityManager.flush();

            return result > 0;

        } catch (Exception e) {
            System.err.println("Erreur sauvegarde planning: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 📊 GÉNÉRATION COMPLÈTE avec transactions par lot
     */
    @Transactional
    public Map<String, Object> generatePlanningBatch(String dateDebut, int nombreEmployes, int tempsParCarte) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. Récupérer commandes
            String sqlCommandes = """
                SELECT HEX(o.id) as order_id, 
                       o.num_commande,
                       o.priority,
                       o.estimated_time_minutes,
                       COUNT(cco.card_certification_id) as nb_cartes
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
                LIMIT 15
            """;

            Query commandesQuery = entityManager.createNativeQuery(sqlCommandes);
            @SuppressWarnings("unchecked")
            List<Object[]> commandes = commandesQuery.getResultList();

            // 2. Récupérer employés
            String sqlEmployes = "SELECT HEX(id), prenom, nom FROM j_employee WHERE actif = 1 LIMIT ?";
            Query empQuery = entityManager.createNativeQuery(sqlEmployes);
            empQuery.setParameter(1, nombreEmployes);

            @SuppressWarnings("unchecked")
            List<Object[]> employes = empQuery.getResultList();

            if (employes.isEmpty()) {
                result.put("success", false);
                result.put("error", "Aucun employé actif");
                return result;
            }

            // 3. Planification
            List<Map<String, Object>> planifications = new ArrayList<>();
            LocalDate datePlan = LocalDate.parse(dateDebut);
            int sauvegardes = 0;
            int employeIndex = 0;
            int heureDebut = 9;

            for (Object[] commande : commandes) {
                String orderId = (String) commande[0];
                String numCommande = (String) commande[1];
                String priority = (String) commande[2];
                Integer estimatedTime = (Integer) commande[3];
                Long nbCartes = ((Number) commande[4]).longValue();

                Object[] employe = employes.get(employeIndex % employes.size());
                String employeId = (String) employe[0];
                String employeNom = employe[1] + " " + employe[2];

                // Calculer durée
                int dureeMinutes = Math.max(60,
                        estimatedTime != null && estimatedTime > 0 ? estimatedTime : (int)(nbCartes * tempsParCarte));

                LocalDateTime startTime = datePlan.atTime(heureDebut, 0);

                // Vérifier débordement
                if (startTime.getHour() + (dureeMinutes / 60) >= 17) {
                    datePlan = datePlan.plusDays(1);
                    heureDebut = 9;
                    startTime = datePlan.atTime(heureDebut, 0);
                }

                // Sauvegarder
                boolean saved = savePlanning(orderId, employeId, datePlan, startTime, dureeMinutes, priority);
                if (saved) {
                    sauvegardes++;
                }

                // Données de retour
                Map<String, Object> planning = new HashMap<>();
                planning.put("num_commande", numCommande);
                planning.put("employee_name", employeNom);
                planning.put("planning_date", datePlan.toString());
                planning.put("start_time", startTime.toString());
                planning.put("duration_minutes", dureeMinutes);
                planning.put("priority", priority);
                planning.put("saved", saved);

                planifications.add(planning);

                // Avancer
                employeIndex++;
                heureDebut += 2; // 2h entre chaque commande

                if (heureDebut >= 16) {
                    datePlan = datePlan.plusDays(1);
                    heureDebut = 9;
                    employeIndex = 0;
                }
            }

            result.put("success", true);
            result.put("planifications_creees", planifications.size());
            result.put("planifications_sauvees", sauvegardes);
            result.put("planifications", planifications);
            result.put("employes_actifs", employes.size());

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 🧪 TEST SIMPLE
     */
    @Transactional
    public Map<String, Object> testSimple() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Données de test fixe
            String orderId = "01972AF4D06B5DFC50435B900B38E6C9"; // QCOFOAWCD
            String employeeId = "08C68C835C84420A88E7AEB56BFA8E6A"; // Ibrahim ALAME
            LocalDate datePlan = LocalDate.now().plusDays(1);
            LocalDateTime startTime = datePlan.atTime(15, 30);

            boolean saved = savePlanning(orderId, employeeId, datePlan, startTime, 90, "HIGH");

            result.put("success", saved);
            result.put("test_order", "QCOFOAWCD");
            result.put("test_employee", "Ibrahim ALAME");
            result.put("test_date", datePlan.toString());
            result.put("test_time", startTime.toString());
            result.put("message", saved ? "Test réussi" : "Test échoué");

            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 📋 CONSULTATION des plannings
     */
    public List<Map<String, Object>> getPlanning(String date) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append("    HEX(p.id) as planning_id,");
            sql.append("    p.planning_date,");
            sql.append("    p.start_time,");
            sql.append("    p.estimated_duration_minutes,");
            sql.append("    p.priority,");
            sql.append("    p.status,");
            sql.append("    o.num_commande,");
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
                item.put("planning_id", row[0]);
                item.put("planning_date", row[1]);
                item.put("start_time", row[2]);
                item.put("duration_minutes", row[3]);
                item.put("priority", row[4]);
                item.put("status", row[5]);
                item.put("num_commande", row[6]);
                item.put("employee_name", row[7]);
                planning.add(item);
            }

            return planning;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
