package com.pcagrade.order.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GreedyPlanningService {

    public Map<String, Object> executeGreedyPlanning(int day, int month, int year) {
        try {
            System.out.println("🎲 === GREEDY PLANNING ===");

            Map<String, Object> result = new HashMap<>();

            // 1. Get active employees
            List<Map<String, Object>> employees = employeeService.getAllActiveEmployees();
            if (employees.isEmpty()) {
                result.put("success", false);
                result.put("message", "❌ No employees available");
                return result;
            }

            // 2. Get orders to plan
            List<Map<String, Object>> orders = orderService.getOrdersPeriod(day, month, year);

            // 3. Greedy algorithm
            List<Map<String, Object>> createdPlannings = new ArrayList<>();
            int employeeIndex = 0;

            for (Map<String, Object> order : orders) {
                Map<String, Object> employee = employees.get(employeeIndex % employees.size());
                String employeeId = (String) employee.get("id");
                String employeeName = employee.get("firstName") + " " + employee.get("lastName");

                // Calculate duration
                Integer cardCount = (Integer) order.get("cardCount");
                int durationMinutes = Math.max(60, 30 + cardCount * 3);

                Map<String, Object> planning = new HashMap<>();
                planning.put("order_id", order.get("id"));
                planning.put("employee_id", employeeId);
                planning.put("employee_name", employeeName);
                planning.put("duration_minutes", durationMinutes);

                createdPlannings.add(planning);
                employeeIndex++;
            }

            result.put("success", true);
            result.put("plannings", createdPlannings);
            return result;

        } catch (Exception e) {
            System.err.println("❌ Planning error: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return error;
        }
    }
}
