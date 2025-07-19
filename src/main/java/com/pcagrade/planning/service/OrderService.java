package com.pcagrade.planning.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    public List<Map<String, Object>> getOrdersPeriod(
            int startDay, int startMonth, int startYear,
            int endDay, int endMonth, int endYear) {

        String sqlOrders = """
            SELECT 
                HEX(o.id) as id, 
                o.order_number as orderNumber,
                o.date as receptionDate,
                o.card_count as cardCount,
                o.priority_string as priority,
                o.total_price as totalPrice,
                o.status,
                o.estimated_time_minutes as estimatedTimeMinutes
            FROM `order` o
            WHERE o.date >= ? AND o.date <= ?
            ORDER BY o.date ASC
            """;
    }

    public Map<String, Object> calculateOrderStatistics(String orderId) {
        // Calculate statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("cardCount", cardCount);
        stats.put("cardsWithName", cardsWithName);
        stats.put("percentageWithName", percentage);
        return stats;
    }
}