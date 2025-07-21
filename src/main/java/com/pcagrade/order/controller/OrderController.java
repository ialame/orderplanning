package com.pcagrade.order.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pcagrade.order.service.OrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import com.github.f4b6a3.ulid.Ulid;
import com.pcagrade.order.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/frontend/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrdersFrontend() {
        try {
            System.out.println("📋 Frontend: Retrieving orders with real data");

            // Use the correct method that returns List<Map<String, Object>>
            List<Map<String, Object>> orders = orderService.getRecentOrdersAsMap();

            System.out.println("✅ " + orders.size() + " orders returned");
            return ResponseEntity.ok(orders);

        } catch (Exception e) {
            System.err.println("❌ Error retrieving orders: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping("/frontend/orders/{id}/cards")
    public ResponseEntity<Map<String, Object>> getOrderCards(@PathVariable String id) {
        try {
            System.out.println("🃏 Frontend: Retrieving cards for order: " + id);

            String sql = """
            SELECT 
                HEX(cc.id) as cardId,
                cc.barcode as barcode,
                COALESCE(cc.type, 'Pokemon') as type,
                cc.card_id as cardId,
                COALESCE(cc.annotation, '') as annotation,
                COALESCE(ct.name, CONCAT('Pokemon Card ', cc.barcode)) as name
            FROM card_certification_order cco
            INNER JOIN card_certification cc ON cco.card_certification_id = cc.id
            LEFT JOIN card_translation ct ON cc.card_id = ct.translatable_id 
            WHERE HEX(cco.order_id) = ?
            """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, id.replace("-", ""));

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> cards = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> card = Map.of(
                        "cardId", (String) row[0],
                        "barcode", (String) row[1],
                        "type", (String) row[2],
                        "annotation", (String) row[4],
                        "name", (String) row[5]
                );
                cards.add(card);
            }

            System.out.println("✅ " + cards.size() + " cards found for order " + id);
            return ResponseEntity.ok(Map.of(
                    "orderId", id,
                    "cards", cards,
                    "totalCards", cards.size()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error loading cards: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all orders with pagination support
     */
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            System.out.println("📋 Retrieving all orders - page: " + page + ", size: " + size);

            List<Map<String, Object>> orders = orderService.getAllOrdersAsMap();

            // Simple pagination
            int start = page * size;
            int end = Math.min(start + size, orders.size());

            if (start >= orders.size()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<Map<String, Object>> paginatedOrders = orders.subList(start, end);

            System.out.println("✅ Returning " + paginatedOrders.size() + " orders (page " + page + ")");
            return ResponseEntity.ok(paginatedOrders);

        } catch (Exception e) {
            System.err.println("❌ Error retrieving all orders: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Get orders for planning since a specific date
     */
    @GetMapping("/planning")
    public ResponseEntity<List<Map<String, Object>>> getOrdersForPlanning(
            @RequestParam int day,
            @RequestParam int month,
            @RequestParam int year) {
        try {
            System.out.println("📋 Retrieving orders for planning since: " + day + "/" + month + "/" + year);

            List<Map<String, Object>> orders = orderService.getOrdersForPlanning(day, month, year);

            System.out.println("✅ " + orders.size() + " orders found for planning");
            return ResponseEntity.ok(orders);

        } catch (Exception e) {
            System.err.println("❌ Error retrieving orders for planning: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Get order statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        try {
            System.out.println("📊 Retrieving order statistics");

            Map<String, Object> statistics = orderService.getOrderStatistics();

            System.out.println("✅ Order statistics retrieved successfully");
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            System.err.println("❌ Error retrieving order statistics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Search orders by various criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchOrders(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        try {
            System.out.println("🔍 Searching orders with criteria: " + searchTerm + ", " + status + ", " + priority);

            // Convert string parameters to enums if provided
            Order.OrderStatus orderStatus = null;
            if (status != null && !status.isEmpty()) {
                try {
                    orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid status: " + status);
                }
            }

            Order.OrderPriority orderPriority = null;
            if (priority != null && !priority.isEmpty()) {
                try {
                    orderPriority = Order.OrderPriority.valueOf(priority.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid priority: " + priority);
                }
            }

            List<Order> orders = orderService.searchOrders(searchTerm, orderStatus, orderPriority);
            List<Map<String, Object>> orderMaps = orders.stream()
                    .map(order -> {
                        Map<String, Object> orderMap = Map.of(
                                "id", order.getId().toString(),
                                "orderNumber", order.getOrderNumber(),
                                "customerName", order.getCustomerName(),
                                "orderDate", order.getOrderDate(),
                                "status", order.getStatus().name(),
                                "priority", order.getPriority().name(),
                                "cardCount", order.getCardCount(),
                                "totalPrice", order.getTotalPrice()
                        );
                        return orderMap;
                    })
                    .toList();

            System.out.println("✅ " + orderMaps.size() + " orders found matching criteria");
            return ResponseEntity.ok(orderMaps);

        } catch (Exception e) {
            System.err.println("❌ Error searching orders: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
}