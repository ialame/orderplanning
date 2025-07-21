package com.pcagrade.order.service;

import com.pcagrade.order.entity.Order;
import com.pcagrade.order.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing orders and order-related operations
 * Translated from CommandeService to OrderService with enhanced functionality
 */
@Service
@Transactional
@Validated
@Slf4j
public class OrderService {

    private static final int DEFAULT_PROCESSING_TIME_PER_CARD = 3; // minutes per card
    private static final int MAX_CARDS_PER_ORDER = 1000;
    private static final int MIN_CARDS_PER_ORDER = 1;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    // ========== CRUD OPERATIONS ==========

    /**
     * Create a new order
     * @param order the order to create
     * @return created order
     */
    public Order createOrder(@Valid @NotNull Order order) {
        log.info("Creating new order: {}", order.getOrderNumber());

        // Validate business rules
        validateNewOrder(order);

        // Set default values
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        if (order.getEstimatedTimeMinutes() == null && order.getCardCount() != null) {
            order.setEstimatedTimeMinutes(calculateEstimatedTime(order.getCardCount()));
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }
        if (order.getPriority() == null) {
            order.setPriority(OrderPriority.MEDIUM);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    /**
     * Update an existing order
     * @param order the order to update
     * @return updated order
     */
    public Order updateOrder(@Valid @NotNull Order order) {
        log.info("Updating order: {}", order.getId());

        if (!orderRepository.existsById(order.getId())) {
            throw new IllegalArgumentException("Order not found with ID: " + order.getId());
        }

        // Recalculate estimated time if card count changed
        if (order.getCardCount() != null && order.getEstimatedTimeMinutes() == null) {
            order.setEstimatedTimeMinutes(calculateEstimatedTime(order.getCardCount()));
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated successfully: {}", updatedOrder.getId());
        return updatedOrder;
    }

    /**
     * Find order by ID
     * @param id the order ID
     * @return optional order
     */
    @Transactional(readOnly = true)
    public Optional<Order> findById(@NotNull UUID id) {
        return orderRepository.findById(id);
    }

    /**
     * Find order by order number
     * @param orderNumber the order number
     * @return optional order
     */
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNumber(@NotNull String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    /**
     * Get all orders with pagination
     * @param pageable pagination information
     * @return page of orders
     */
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * Delete an order
     * @param id the order ID
     */
    public void deleteOrder(@NotNull UUID id) {
        log.info("Deleting order: {}", id);

        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("Order not found with ID: " + id);
        }

        orderRepository.deleteById(id);
        log.info("Order deleted successfully: {}", id);
    }

    // ========== ORDER STATUS OPERATIONS ==========

    /**
     * Find orders by status
     * @param status the order status
     * @return list of orders
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByStatus(@NotNull OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Find pending orders (status = PENDING)
     * @return list of pending orders
     */
    @Transactional(readOnly = true)
    public List<Order> findPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }

    /**
     * Find in progress orders (status = IN_PROGRESS)
     * @return list of in progress orders
     */
    @Transactional(readOnly = true)
    public List<Order> findInProgressOrders() {
        return orderRepository.findByStatus(OrderStatus.IN_PROGRESS);
    }

    /**
     * Find completed orders (status = COMPLETED)
     * @return list of completed orders
     */
    @Transactional(readOnly = true)
    public List<Order> findCompletedOrders() {
        return orderRepository.findByStatus(OrderStatus.COMPLETED);
    }

    /**
     * Update order status
     * @param id the order ID
     * @param status the new status
     * @return updated order
     */
    public Order updateOrderStatus(@NotNull UUID id, @NotNull OrderStatus status) {
        log.info("Updating order {} status to {}", id, status);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        order.setStatus(status);

        // Set completion date if completed
        if (status == OrderStatus.COMPLETED && order.getCompletionDate() == null) {
            order.setCompletionDate(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully");
        return updatedOrder;
    }

    // ========== BUSINESS OPERATIONS ==========

    /**
     * Get recent orders (for frontend display)
     * @return list of recent orders as maps
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentOrders() {
        try {
            log.info("Fetching recent orders for frontend display");

            String sql = """
                SELECT 
                    HEX(o.id) as id,
                    o.order_number as orderNumber,
                    o.card_count as cardCount,
                    o.priority as priority,
                    o.status as status,
                    o.total_price as totalPrice,
                    o.estimated_time_minutes as estimatedTimeMinutes,
                    o.order_date as orderDate,
                    o.creation_date as creationDate,
                    o.completion_date as completionDate,
                    o.customer_name as customerName,
                    o.description as description
                FROM `order` o
                ORDER BY o.creation_date DESC
                LIMIT 100
                """;

            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> orders = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", (String) row[0]);
                orderData.put("orderNumber", (String) row[1]);
                orderData.put("cardCount", row[2]);
                orderData.put("priority", (String) row[3]);
                orderData.put("status", (String) row[4]);
                orderData.put("totalPrice", row[5]);
                orderData.put("estimatedTimeMinutes", row[6]);
                orderData.put("orderDate", row[7]);
                orderData.put("creationDate", row[8]);
                orderData.put("completionDate", row[9]);
                orderData.put("customerName", (String) row[10]);
                orderData.put("description", (String) row[11]);

                // Add calculated fields
                Integer cardCount = (Integer) row[2];
                Integer estimatedTime = (Integer) row[6];

                if (cardCount != null) {
                    orderData.put("estimatedHours", Math.round((estimatedTime != null ? estimatedTime : cardCount * 3) / 60.0 * 100.0) / 100.0);
                    orderData.put("complexityLevel", getComplexityLevel(cardCount));
                }

                // Add status display info
                String status = (String) row[4];
                orderData.put("statusDisplay", getStatusDisplay(status));
                orderData.put("statusColor", getStatusColor(status));
                orderData.put("isCompleted", "COMPLETED".equals(status));
                orderData.put("isPending", "PENDING".equals(status));
                orderData.put("isInProgress", "IN_PROGRESS".equals(status));

                orders.add(orderData);
            }

            log.info("Successfully fetched {} recent orders", orders.size());
            return orders;

        } catch (Exception e) {
            log.error("Error fetching recent orders: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get all orders as map data (for frontend compatibility)
     * @return list of order data maps
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllOrdersAsMap() {
        return getRecentOrders(); // For now, same implementation
    }

    /**
     * Get orders for planning from a specific date
     * @param jour day
     * @param mois month
     * @param annee year
     * @return list of order data maps
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrdersForPlanning(int jour, int mois, int annee) {
        try {
            LocalDate fromDate = LocalDate.of(annee, mois, jour);
            log.info("Fetching orders for planning from date: {}", fromDate);

            String sql = """
                SELECT 
                    HEX(o.id) as id,
                    o.order_number as orderNumber,
                    o.card_count as cardCount,
                    o.priority as priority,
                    o.status as status,
                    o.estimated_time_minutes as estimatedTimeMinutes,
                    o.order_date as orderDate,
                    o.creation_date as creationDate
                FROM `order` o
                WHERE o.order_date >= ? 
                  AND o.status IN ('PENDING', 'IN_PROGRESS')
                ORDER BY 
                    CASE o.priority 
                        WHEN 'HIGH' THEN 1 
                        WHEN 'MEDIUM' THEN 2 
                        WHEN 'LOW' THEN 3 
                        ELSE 4 
                    END,
                    o.order_date ASC,
                    o.creation_date ASC
                """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, fromDate);
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> orders = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", (String) row[0]);
                orderData.put("numeroCommande", (String) row[1]); // Legacy compatibility
                orderData.put("orderNumber", (String) row[1]);
                orderData.put("nombreCartes", row[2]); // Legacy compatibility
                orderData.put("cardCount", row[2]);
                orderData.put("priorite", (String) row[3]); // Legacy compatibility
                orderData.put("priority", (String) row[3]);
                orderData.put("statut", (String) row[4]); // Legacy compatibility
                orderData.put("status", (String) row[4]);
                orderData.put("dureeEstimeeMinutes", row[5]); // Legacy compatibility
                orderData.put("estimatedTimeMinutes", row[5]);

                orders.add(orderData);
            }

            log.info("Found {} orders for planning from {}", orders.size(), fromDate);
            return orders;

        } catch (Exception e) {
            log.error("Error fetching orders for planning: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Count orders by status
     * @param status the order status
     * @return count of orders
     */
    @Transactional(readOnly = true)
    public long countOrdersByStatus(@NotNull OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Get order statistics
     * @return statistics map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Basic counts
            long totalOrders = orderRepository.count();
            long pendingCount = countOrdersByStatus(OrderStatus.PENDING);
            long inProgressCount = countOrdersByStatus(OrderStatus.IN_PROGRESS);
            long completedCount = countOrdersByStatus(OrderStatus.COMPLETED);

            stats.put("totalOrders", totalOrders);
            stats.put("pendingOrders", pendingCount);
            stats.put("inProgressOrders", inProgressCount);
            stats.put("completedOrders", completedCount);

            // Calculate completion rate
            if (totalOrders > 0) {
                double completionRate = (double) completedCount / totalOrders * 100;
                stats.put("completionRatePercent", Math.round(completionRate * 100.0) / 100.0);
            } else {
                stats.put("completionRatePercent", 0.0);
            }

            stats.put("success", true);
            stats.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error calculating order statistics", e);
            stats.put("success", false);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    // ========== UTILITY METHODS ==========

    /**
     * Calculate estimated processing time based on card count
     * @param cardCount number of cards
     * @return estimated time in minutes
     */
    private int calculateEstimatedTime(int cardCount) {
        return cardCount * DEFAULT_PROCESSING_TIME_PER_CARD;
    }

    /**
     * Validate new order business rules
     * @param order the order to validate
     */
    private void validateNewOrder(Order order) {
        // Validate order number uniqueness
        if (order.getOrderNumber() != null &&
                orderRepository.findByOrderNumber(order.getOrderNumber()).isPresent()) {
            throw new IllegalArgumentException("Order number already exists: " + order.getOrderNumber());
        }

        // Validate card count
        if (order.getCardCount() != null) {
            if (order.getCardCount() < MIN_CARDS_PER_ORDER ||
                    order.getCardCount() > MAX_CARDS_PER_ORDER) {
                throw new IllegalArgumentException(
                        String.format("Card count must be between %d and %d",
                                MIN_CARDS_PER_ORDER, MAX_CARDS_PER_ORDER));
            }
        }
    }

    /**
     * Get complexity level based on card count
     * @param cardCount number of cards
     * @return complexity level string
     */
    private String getComplexityLevel(int cardCount) {
        if (cardCount <= 10) return "LOW";
        else if (cardCount <= 50) return "MEDIUM";
        else if (cardCount <= 100) return "HIGH";
        else return "VERY_HIGH";
    }

    /**
     * Get status display text
     * @param status order status
     * @return display text
     */
    private String getStatusDisplay(String status) {
        return switch (status) {
            case "PENDING" -> "En attente";
            case "IN_PROGRESS" -> "En cours";
            case "COMPLETED" -> "Terminée";
            case "CANCELLED" -> "Annulée";
            default -> status;
        };
    }

    /**
     * Get status color for UI
     * @param status order status
     * @return color string
     */
    private String getStatusColor(String status) {
        return switch (status) {
            case "PENDING" -> "orange";
            case "IN_PROGRESS" -> "blue";
            case "COMPLETED" -> "green";
            case "CANCELLED" -> "red";
            default -> "gray";
        };
    }

    // ========== COMPATIBILITY METHODS (for migration from CommandeService) ==========

    /**
     * Legacy method for compatibility with existing code
     * @deprecated Use getAllOrdersAsMap() instead
     */
    @Deprecated
    public List<Map<String, Object>> getToutesCommandes() {
        log.warn("Using deprecated method getToutesCommandes(), please use getAllOrdersAsMap()");
        return getAllOrdersAsMap();
    }

    /**
     * Legacy method for compatibility with existing code
     * @deprecated Use getOrdersForPlanning() instead
     */
    @Deprecated
    public List<Map<String, Object>> getCommandesDepuis(int jour, int mois, int annee) {
        log.warn("Using deprecated method getCommandesDepuis(), please use getOrdersForPlanning()");
        return getOrdersForPlanning(jour, mois, annee);
    }
}