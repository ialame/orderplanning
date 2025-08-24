package com.pcagrade.order.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Greedy Planning Service - Simple greedy algorithm for task assignment
 */
@Service
@Slf4j
public class GreedyPlanningService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private EntityManager entityManager;
    /**
     * Execute greedy planning algorithm
     * @param day target day
     * @param month target month
     * @param year target year
     * @return planning result
     */
    public Map<String, Object> executeGreedyPlanning(int day, int month, int year) {
        try {
            log.info("🎲 Starting Greedy Planning for date: {}/{}/{}", day, month, year);

            Map<String, Object> result = new HashMap<>();

            // 1. Get active employees
            List<Map<String, Object>> employees =  employeeService.getAllActiveEmployees();
            if (employees.isEmpty()) {
                log.warn("❌ No active employees found");
                result.put("success", false);
                result.put("message", "❌ No employees available");
                return result;
            }
            log.info("Found {} active employees", employees.size());

            // 2. Get orders to plan
            List<Map<String, Object>> orders = orderService.getOrdersForPlanning(day, month, year);
            if (orders.isEmpty()) {
                log.info("No orders found for planning");
                result.put("success", true);
                result.put("message", "No orders to plan");
                result.put("plannings", new ArrayList<>());
                return result;
            }
            log.info("Found {} orders to plan", orders.size());

            // 3. Execute greedy algorithm
            List<Map<String, Object>> createdPlannings = new ArrayList<>();
            int employeeIndex = 0;

// ✅ DEBUG: Log the employees we have
            log.info("🧑‍💼 DEBUG: Found {} employees for round-robin distribution", employees.size());
            for (int i = 0; i < employees.size(); i++) {
                Map<String, Object> emp = employees.get(i);
                log.info("  Employee {}: {} {} (ID: {})", i, emp.get("firstName"), emp.get("lastName"), emp.get("id"));
            }

            for (Map<String, Object> order : orders) {
                // ✅ DEBUG: Log which employee is selected BEFORE the calculation
                int currentEmployeeIndex = employeeIndex % employees.size();
                Map<String, Object> employee = employees.get(currentEmployeeIndex);
                String employeeId = (String) employee.get("id");
                String employeeName = employee.get("firstName") + " " + employee.get("lastName");

                log.info("🔄 DEBUG: Order {} -> Employee index {} ({}% {}) -> Employee: {} (ID: {})",
                        order.get("orderNumber"), employeeIndex, employeeIndex, employees.size(),
                        employeeName, employeeId);

                // Calculate duration based on card count
                Integer cardCount = (Integer) order.get("cardCount");
                if (cardCount == null) {
                    cardCount = (Integer) order.get("nombreCartes"); // Legacy compatibility
                }
                if (cardCount == null) {
                    cardCount = 10; // Default fallback
                }

                int durationMinutes = Math.max(60, 30 + cardCount * 3);

                // ✅ Si vous avez ajouté la vérification de doublons, assurez-vous qu'elle ressemble à ça :
                String orderId = (String) order.get("id");
                if (isPlanningAlreadyExists(orderId, employeeId)) {
                    log.debug("Planning already exists for order {} and employee {}, skipping but continuing rotation",
                            order.get("orderNumber"), employeeName);
                    employeeIndex++; // ✅ IMPORTANT: Incrémenter même en cas de skip
                    continue;
                }

                // Create planning entry data
                Map<String, Object> planning = new HashMap<>();
                planning.put("order_id", orderId);
                planning.put("orderId", orderId);
                planning.put("employee_id", employeeId);
                planning.put("employeeId", employeeId);
                planning.put("employee_name", employeeName);
                planning.put("employeeName", employeeName);
                planning.put("duration_minutes", durationMinutes);
                planning.put("durationMinutes", durationMinutes);
                planning.put("card_count", cardCount);
                planning.put("cardCount", cardCount);
                planning.put("order_number", order.get("orderNumber"));
                planning.put("numeroCommande", order.get("orderNumber"));
                planning.put("priority", order.get("priority"));
                planning.put("priorite", order.get("priority"));

                createdPlannings.add(planning);

                // ✅ DEBUG: Confirmer l'assignation avant d'incrémenter
                log.info("✅ DEBUG: Order {} assigned to employee {} (index {})",
                        order.get("orderNumber"), employeeName, employeeIndex);

                employeeIndex++; // ✅ Incrémenter après l'assignation

                log.debug("Assigned order {} to employee {} (duration: {} minutes)",
                        order.get("orderNumber"), employeeName, durationMinutes);
            }

// ✅ DEBUG: Log final distribution
            log.info("🎯 DEBUG: Final distribution summary:");
            Map<String, Integer> employeeOrderCounts = new HashMap<>();
            for (Map<String, Object> planning : createdPlannings) {
                String empName = (String) planning.get("employee_name");
                employeeOrderCounts.merge(empName, 1, Integer::sum);
            }
            employeeOrderCounts.forEach((name, count) ->
                    log.info("  Employee {}: {} orders assigned", name, count));
// ✅ DEBUG: Log final distribution  FIN
            result.put("success", true);
            result.put("message", String.format("✅ Greedy planning completed: %d assignments created",
                    createdPlannings.size()));
            result.put("plannings", createdPlannings);
            result.put("totalPlannings", createdPlannings.size());
            result.put("totalEmployees", employees.size());
            result.put("totalOrders", orders.size());

            // Add algorithm info
            Map<String, Object> algorithmInfo = new HashMap<>();
            algorithmInfo.put("name", "Greedy Round-Robin");
            algorithmInfo.put("description", "Simple round-robin assignment of orders to employees");
            algorithmInfo.put("complexity", "O(n)");
            result.put("algorithm", algorithmInfo);

            log.info("✅ Greedy planning completed successfully: {} assignments", createdPlannings.size());
            return result;

        } catch (Exception e) {
            log.error("❌ Error in greedy planning: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error in greedy planning: " + e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            return error;
        }
    }

    /**
     * Execute simple greedy planning without date parameters
     * @return planning result
     */
    public Map<String, Object> executeGreedyPlanning() {
        LocalDate today = LocalDate.now();
        return executeGreedyPlanning(today.getDayOfMonth(), today.getMonthValue(), today.getYear());
    }

    /**
     * Get algorithm information
     * @return algorithm details
     */
    public Map<String, Object> getAlgorithmInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Greedy Planning Algorithm");
        info.put("description", "Simple greedy algorithm that assigns orders to employees in round-robin fashion");
        info.put("timeComplexity", "O(n)");
        info.put("spaceComplexity", "O(1)");
        info.put("advantages", List.of(
                "Fast execution",
                "Simple implementation",
                "Predictable behavior",
                "Even workload distribution"
        ));
        info.put("disadvantages", List.of(
                "May not find optimal solution",
                "Doesn't consider employee skills",
                "Ignores priority optimization",
                "No time slot optimization"
        ));
        return info;
    }

    /**
     * Validate planning parameters
     * @param day day of month
     * @param month month (1-12)
     * @param year year
     * @return true if valid
     */
    private boolean isValidDate(int day, int month, int year) {
        try {
            LocalDate.of(year, month, day);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calculate basic statistics for greedy planning
     * @param plannings list of planning entries
     * @param employees list of employees
     * @return statistics map
     */
    private Map<String, Object> calculateGreedyStatistics(List<Map<String, Object>> plannings,
                                                          List<Map<String, Object>> employees) {
        Map<String, Object> stats = new HashMap<>();

        // Total assignments
        stats.put("totalAssignments", plannings.size());

        // Assignments per employee
        Map<String, Integer> assignmentCounts = new HashMap<>();
        int totalDuration = 0;

        for (Map<String, Object> planning : plannings) {
            String employeeId = (String) planning.get("employee_id");
            assignmentCounts.merge(employeeId, 1, Integer::sum);

            Integer duration = (Integer) planning.get("duration_minutes");
            if (duration != null) {
                totalDuration += duration;
            }
        }

        stats.put("assignmentCounts", assignmentCounts);
        stats.put("totalDurationMinutes", totalDuration);
        stats.put("averageDurationMinutes", plannings.isEmpty() ? 0 : totalDuration / plannings.size());

        // Employee utilization
        if (!employees.isEmpty()) {
            stats.put("employeesUsed", assignmentCounts.size());
            stats.put("employeeUtilizationPercent",
                    Math.round((double) assignmentCounts.size() / employees.size() * 100));
        }

        return stats;
    }
    /**
     * Check if planning already exists for this order and employee
     * @param orderId the order ID
     * @param employeeId the employee ID
     * @return true if planning exists, false otherwise
     */
    private boolean isPlanningAlreadyExists(String orderId, String employeeId) {
        try {
            // Check in your planning table (adjust table name if needed)
            String checkSql = """
            SELECT COUNT(*) FROM j_planning 
            WHERE order_id = UNHEX(?) AND employee_id = UNHEX(?)
            """;

            Query query = entityManager.createNativeQuery(checkSql);
            query.setParameter(1, orderId.replace("-", ""));
            query.setParameter(2, employeeId.replace("-", ""));

            Number count = (Number) query.getSingleResult();
            return count.intValue() > 0;

        } catch (Exception e) {
            log.warn("Error checking existing planning: {}", e.getMessage());
            return false; // In case of error, assume it doesn't exist
        }
    }
}