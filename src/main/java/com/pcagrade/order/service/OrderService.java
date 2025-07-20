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
            order.setStatus(Order.OrderStatus.PENDING);
        }
        if (order.getEstimatedTimeMinutes() == null && order.getCardCount() != null) {
            order.setEstimatedTimeMinutes(calculateEstimatedTime(order.getCardCount()));
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }
        if (order.getPriority() == null) {
            order.setPriority(Order.OrderPriority.MEDIUM);
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
    public List<Order> findOrdersByStatus(@NotNull Order.OrderStatus status) {
        return orderRepository.findOrdersByStatus(status.getCode());
    }

    /**
     * Find pending orders (status = PENDING)
     * @return list of pending orders
     */
    @Transactional(readOnly = true)
    public List<Order> findPendingOrders() {
        return orderRepository.findPendingOrders();
    }

    /**
     * Find in progress orders (status = IN_PROGRESS)
     * @return list of in progress orders
     */
    @Transactional(readOnly = true)
    public List<Order> findInProgressOrders() {
        return orderRepository.findInProgressOrders();
    }

    /**
     * Find completed orders (status = COMPLETED)
     * @return list of completed orders
     */
    @Transactional(readOnly = true)
    public List<Order> findCompletedOrders() {
        return orderRepository.findCompletedOrders();
    }

    /**
     * Update order status
     * @param orderId the order ID
     * @param newStatus the new status
     * @return updated order
     */
    public Order updateOrderStatus(@NotNull UUID orderId, @NotNull Order.OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        // Set completion date if marking as completed
        if (newStatus == Order.OrderStatus.COMPLETED && order.getCompletionDate() == null) {
            order.setCompletionDate(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    // ========== PLANNING AND SCHEDULING ==========

    /**
     * Find orders that need planning (unassigned orders)
     * @return list of unassigned orders
     */
    @Transactional(readOnly = true)
    public List<Order> findUnassignedOrders() {
        return orderRepository.findUnassignedOrders(Order.OrderStatus.PENDING.getCode());
    }

    /**
     * Find orders to process (pending and in progress)
     * @return list of orders to process
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersToProcess() {
        return orderRepository.findOrdersToProcess();
    }

    /**
     * Find orders created after specific date for planning
     * @param date the date after which to search
     * @return list of orders
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersForPlanningAfterDate(@NotNull LocalDate date) {
        return orderRepository.findOrdersAfterDate(date);
    }

    /**
     * Get orders for planning since specific date (for planning algorithms)
     * @param day day of month
     * @param month month
     * @param year year
     * @return list of order data maps for planning
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrdersForPlanningFromDate(int day, int month, int year) {
        try {
            LocalDate fromDate = LocalDate.of(year, month, day);
            log.info("Fetching orders for planning from date: {}", fromDate);

            String sql = """
                SELECT 
                    HEX(o.id) as id,
                    o.order_number as orderNumber,
                    o.card_count as cardCount,
                    COALESCE(o.estimated_time_minutes, ?) as estimatedTimeMinutes,
                    o.priority as priority,
                    o.status as status,
                    o.order_date as orderDate,
                    o.deadline as deadline,
                    o.total_price as totalPrice
                FROM j_order o
                WHERE o.order_date >= ?
                AND o.status IN (?, ?)
                AND COALESCE(o.cancelled, 0) = 0
                ORDER BY 
                    CASE o.priority 
                        WHEN 'URGENT' THEN 1
                        WHEN 'HIGH' THEN 2
                        WHEN 'MEDIUM' THEN 3
                        WHEN 'LOW' THEN 4
                        ELSE 5
                    END,
                    o.order_date ASC
                """;

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, DEFAULT_PROCESSING_TIME_PER_CARD * 10); // Default for null estimated time
            query.setParameter(2, fromDate);
            query.setParameter(3, Order.OrderStatus.PENDING.getCode());
            query.setParameter(4, Order.OrderStatus.IN_PROGRESS.getCode());

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            List<Map<String, Object>> orders = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", (String) row[0]);
                orderData.put("orderNumber", (String) row[1]);
                orderData.put("cardCount", row[2]);
                orderData.put("estimatedTimeMinutes", row[3]);
                orderData.put("priority", (String) row[4]);
                orderData.put("status", row[5]);
                orderData.put("orderDate", row[6]);
                orderData.put("deadline", row[7]);
                orderData.put("totalPrice", row[8]);

                // Add calculated fields for planning
                Integer cardCount = (Integer) row[2];
                if (cardCount != null) {
                    orderData.put("actualCardCount", cardCount);
                    orderData.put("calculatedDuration", calculateEstimatedTime(cardCount));
                }

                orders.add(orderData);
            }

            log.info("Found {} orders for planning from {}", orders.size(), fromDate);
            return orders;

        } catch (Exception e) {
            log.error("Error fetching orders for planning from {}/{}/{}: {}", day, month, year, e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== STATISTICS AND ANALYTICS ==========

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
            long pendingCount = orderRepository.countByStatus(Order.OrderStatus.PENDING.getCode());
            long inProgressCount = orderRepository.countByStatus(Order.OrderStatus.IN_PROGRESS.getCode());
            long completedCount = orderRepository.countByStatus(Order.OrderStatus.COMPLETED.getCode());

            // Card statistics
            Long totalCards = orderRepository.getTotalCardCount();
            Double avgProcessingTime = orderRepository.getAverageProcessingTime();

            stats.put("totalOrders", totalOrders);
            stats.put("pendingOrders", pendingCount);
            stats.put("inProgressOrders", inProgressCount);
            stats.put("completedOrders", completedCount);
            stats.put("totalCards", totalCards != null ? totalCards : 0L);
            stats.put("averageProcessingTime", avgProcessingTime != null ? avgProcessingTime : 0.0);

            // Calculate completion rate
            if (totalOrders > 0) {
                double completionRate = (completedCount * 100.0) / totalOrders;
                stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            } else {
                stats.put("completionRate", 0.0);
            }

            // Orders with approaching deadlines (next 7 days)
            List<Order> approachingDeadlines = orderRepository.findOrdersWithApproachingDeadlines(7);
            stats.put("ordersWithApproachingDeadlines", approachingDeadlines.size());

            stats.put("timestamp", LocalDateTime.now());
            stats.put("status", "success");

        } catch (Exception e) {
            log.error("Error calculating order statistics: {}", e.getMessage());
            stats.put("status", "error");
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Get orders by priority distribution
     * @return priority distribution map
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getOrdersByPriorityDistribution() {
        Map<String, Long> distribution = new HashMap<>();

        for (Order.OrderPriority priority : Order.OrderPriority.values()) {
            List<Order> orders = orderRepository.findByPriorityOrderByOrderDateAsc(priority.name());
            distribution.put(priority.name(), (long) orders.size());
        }

        return distribution;
    }

    /**
     * Find orders with approaching deadlines
     * @param days number of days to look ahead
     * @return list of orders with approaching deadlines
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersWithApproachingDeadlines(@Positive int days) {
        return orderRepository.findOrdersWithApproachingDeadlines(days);
    }

    // ========== UTILITY METHODS ==========

    /**
     * Calculate estimated processing time based on card count
     * @param cardCount number of cards
     * @return estimated time in minutes
     */
    public int calculateEstimatedTime(@Positive int cardCount) {
        return cardCount * DEFAULT_PROCESSING_TIME_PER_CARD;
    }

    /**
     * Generate unique order number
     * @return unique order number
     */
    public String generateOrderNumber() {
        String prefix = "ORD";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = timestamp.substring(timestamp.length() - 6); // Last 6 digits
        return prefix + "-" + LocalDate.now().getYear() + "-" + suffix;
    }

    /**
     * Validate new order before creation
     * @param order the order to validate
     */
    private void validateNewOrder(Order order) {
        // Check if order number is unique
        if (order.getOrderNumber() != null && orderRepository.findByOrderNumber(order.getOrderNumber()).isPresent()) {
            throw new IllegalArgumentException("Order number already exists: " + order.getOrderNumber());
        }

        // Validate card count range
        if (order.getCardCount() != null) {
            if (order.getCardCount() < MIN_CARDS_PER_ORDER) {
                throw new IllegalArgumentException("Card count must be at least " + MIN_CARDS_PER_ORDER);
            }
            if (order.getCardCount() > MAX_CARDS_PER_ORDER) {
                throw new IllegalArgumentException("Card count cannot exceed " + MAX_CARDS_PER_ORDER);
            }
        }

        // Validate deadline is in the future
        if (order.getDeadline() != null && order.getDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }
    }

    /**
     * Validate status transition
     * @param currentStatus current order status
     * @param newStatus new order status
     */
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Define valid transitions
        Set<Order.OrderStatus> validTransitions = new HashSet<>();

        if (currentStatus != null) {
            switch (currentStatus) {
                case PENDING:
                    validTransitions.addAll(Arrays.asList(
                            Order.OrderStatus.IN_PROGRESS,
                            Order.OrderStatus.CANCELLED
                    ));
                    break;
                case IN_PROGRESS:
                    validTransitions.addAll(Arrays.asList(
                            Order.OrderStatus.COMPLETED,
                            Order.OrderStatus.CANCELLED,
                            Order.OrderStatus.PENDING // Allow going back to pending if needed
                    ));
                    break;
                case COMPLETED:
                    // Generally, completed orders shouldn't change status
                    // But allow reopening if needed
                    validTransitions.add(Order.OrderStatus.IN_PROGRESS);
                    break;
                case CANCELLED:
                    // Allow reactivating cancelled orders
                    validTransitions.add(Order.OrderStatus.PENDING);
                    break;
            }
        } else {
            // If no current status, allow any status
            validTransitions.addAll(Arrays.asList(Order.OrderStatus.values()));
        }

        if (!validTransitions.contains(newStatus)) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    // ========== SEARCH AND FILTERING ==========

    /**
     * Search orders by various criteria
     * @param searchCriteria search criteria map
     * @return list of matching orders
     */
    @Transactional(readOnly = true)
    public List<Order> searchOrders(Map<String, Object> searchCriteria) {
        // This would typically use Criteria API or QueryDSL for complex searches
        // For now, implement basic filtering

        List<Order> allOrders = orderRepository.findAll();

        return allOrders.stream()
                .filter(order -> matchesSearchCriteria(order, searchCriteria))
                .collect(Collectors.toList());
    }

    /**
     * Check if order matches search criteria
     * @param order the order to check
     * @param criteria search criteria
     * @return true if matches
     */
    private boolean matchesSearchCriteria(Order order, Map<String, Object> criteria) {
        for (Map.Entry<String, Object> criterion : criteria.entrySet()) {
            String key = criterion.getKey();
            Object value = criterion.getValue();

            if (value == null) continue;

            switch (key.toLowerCase()) {
                case "status":
                    if (order.getStatus() != null && !order.getStatus().name().equalsIgnoreCase(value.toString())) {
                        return false;
                    }
                    break;
                case "priority":
                    if (order.getPriority() != null && !order.getPriority().name().equalsIgnoreCase(value.toString())) {
                        return false;
                    }
                    break;
                case "ordernumber":
                    if (order.getOrderNumber() != null && !order.getOrderNumber().toLowerCase().contains(value.toString().toLowerCase())) {
                        return false;
                    }
                    break;
                case "mincardsource":
                    if (order.getCardCount() != null && order.getCardCount() < (Integer) value) {
                        return false;
                    }
                    break;
                case "maxcards":
                    if (order.getCardCount() != null && order.getCardCount() > (Integer) value) {
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    // ========== BATCH OPERATIONS ==========

    /**
     * Batch update order statuses
     * @param orderIds list of order IDs
     * @param newStatus new status to set
     * @return number of updated orders
     */
    public int batchUpdateOrderStatus(List<UUID> orderIds, Order.OrderStatus newStatus) {
        log.info("Batch updating {} orders to status {}", orderIds.size(), newStatus);

        int updatedCount = 0;
        for (UUID orderId : orderIds) {
            try {
                updateOrderStatus(orderId, newStatus);
                updatedCount++;
            } catch (Exception e) {
                log.warn("Failed to update order {} to status {}: {}", orderId, newStatus, e.getMessage());
            }
        }

        log.info("Successfully updated {} out of {} orders", updatedCount, orderIds.size());
        return updatedCount;
    }

    /**
     * Get all orders (for migration or batch operations)
     * @return list of all orders
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}