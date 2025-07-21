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
        return orderRepository.findByStatus(status);
    }

    /**
     * Find pending orders (status = PENDING)
     * @return list of pending orders
     */
    @Transactional(readOnly = true)
    public List<Order> findPendingOrders() {
        return orderRepository.findUnassignedOrders(Order.OrderStatus.PENDING);
    }

    /**
     * Find in progress orders (status = IN_PROGRESS)
     * @return list of in progress orders
     */
    @Transactional(readOnly = true)
    public List<Order> findInProgressOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.IN_PROGRESS);
    }

    /**
     * Find completed orders (status = COMPLETED)
     * @return list of completed orders
     */
    @Transactional(readOnly = true)
    public List<Order> findCompletedOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.COMPLETED);
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

        // Set processing dates based on status
        if (newStatus == Order.OrderStatus.IN_PROGRESS && order.getProcessingStartDate() == null) {
            order.setProcessingStartDate(LocalDateTime.now());
        } else if (newStatus == Order.OrderStatus.COMPLETED && order.getProcessingEndDate() == null) {
            order.setProcessingEndDate(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: {} -> {}", orderId, newStatus);
        return updatedOrder;
    }

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Calculate estimated processing time
     * @param cardCount number of cards
     * @return estimated time in minutes
     */
    public int calculateEstimatedTime(@Positive int cardCount) {
        return cardCount * DEFAULT_PROCESSING_TIME_PER_CARD;
    }

    /**
     * Get orders that need planning from a specific date
     * @param day day of the month
     * @param month month (1-12)
     * @param year year
     * @return list of orders as maps for compatibility
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrdersForPlanning(int day, int month, int year) {
        try {
            LocalDate fromDate = LocalDate.of(year, month, day);

            String jpql = """
                SELECT o FROM Order o 
                WHERE o.orderDate >= :fromDate 
                ORDER BY o.priority DESC, o.orderDate ASC
                """;

            Query query = entityManager.createQuery(jpql, Order.class);
            query.setParameter("fromDate", fromDate);

            @SuppressWarnings("unchecked")
            List<Order> orders = query.getResultList();

            List<Map<String, Object>> result = new ArrayList<>();
            for (Order order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId().toString());
                orderMap.put("numeroCommande", order.getOrderNumber());
                orderMap.put("date", order.getOrderDate());
                orderMap.put("delai", calculateDeadlineLabel(order));
                orderMap.put("nombreCartes", order.getCardCount());
                orderMap.put("priorite", order.getPriority().name());
                orderMap.put("prixTotal", order.getTotalPrice());
                orderMap.put("status", order.getStatus().ordinal());
                orderMap.put("dateCreation", order.getCreationDate());
                orderMap.put("dateModification", order.getModificationDate());

                result.add(orderMap);
            }

            log.info("✅ {} orders loaded from {}/{}/{}", result.size(), day, month, year);
            return result;

        } catch (Exception e) {
            log.error("❌ Error loading orders: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get all orders as map for compatibility
     * @return list of orders as maps
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllOrdersAsMap() {
        try {
            List<Order> orders = orderRepository.findAll();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Order order : orders) {
                Map<String, Object> orderMap = convertOrderToMap(order);
                result.add(orderMap);
            }

            return result;

        } catch (Exception e) {
            log.error("Error retrieving all orders", e);
            return new ArrayList<>();
        }
    }

    // ========== VALIDATION METHODS ==========

    /**
     * Validate new order business rules
     */
    private void validateNewOrder(Order order) {
        if (order.getCardCount() != null) {
            if (order.getCardCount() < MIN_CARDS_PER_ORDER || order.getCardCount() > MAX_CARDS_PER_ORDER) {
                throw new IllegalArgumentException(
                        String.format("Card count must be between %d and %d", MIN_CARDS_PER_ORDER, MAX_CARDS_PER_ORDER)
                );
            }
        }

        if (order.getOrderNumber() != null && orderRepository.findByOrderNumber(order.getOrderNumber()).isPresent()) {
            throw new IllegalArgumentException("Order number already exists: " + order.getOrderNumber());
        }
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Define valid transitions
        Set<Order.OrderStatus> validTransitions = new HashSet<>();

        if (currentStatus != null) {
            switch (currentStatus) {
                case PENDING:
                    validTransitions.addAll(Arrays.asList(
                            Order.OrderStatus.SCHEDULED,
                            Order.OrderStatus.IN_PROGRESS,
                            Order.OrderStatus.CANCELLED
                    ));
                    break;
                case SCHEDULED:
                    validTransitions.addAll(Arrays.asList(
                            Order.OrderStatus.IN_PROGRESS,
                            Order.OrderStatus.CANCELLED,
                            Order.OrderStatus.PENDING // Allow going back to pending if needed
                    ));
                    break;
                case IN_PROGRESS:
                    validTransitions.addAll(Arrays.asList(
                            Order.OrderStatus.COMPLETED,
                            Order.OrderStatus.CANCELLED,
                            Order.OrderStatus.SCHEDULED // Allow going back to scheduled if needed
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
     * @param searchTerm search term
     * @param status order status filter
     * @param priority priority filter
     * @return list of filtered orders
     */
    @Transactional(readOnly = true)
    public List<Order> searchOrders(String searchTerm, Order.OrderStatus status, Order.OrderPriority priority) {
        // Implementation would depend on specific search requirements
        // For now, return a basic filtered list
        return orderRepository.findAll().stream()
                .filter(order -> searchTerm == null ||
                        order.getOrderNumber().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        order.getCustomerName().toLowerCase().contains(searchTerm.toLowerCase()))
                .filter(order -> status == null || order.getStatus().equals(status))
                .filter(order -> priority == null || order.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

    // ========== STATISTICS METHODS ==========

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
            long pendingCount = orderRepository.countByStatus(Order.OrderStatus.PENDING);
            long inProgressCount = orderRepository.countByStatus(Order.OrderStatus.IN_PROGRESS);
            long completedCount = orderRepository.countByStatus(Order.OrderStatus.COMPLETED);

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
     * Convert Order entity to Map for compatibility
     */
    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.getId().toString());
        orderMap.put("numeroCommande", order.getOrderNumber());
        orderMap.put("date", order.getOrderDate());
        orderMap.put("delai", calculateDeadlineLabel(order));
        orderMap.put("nombreCartes", order.getCardCount());
        orderMap.put("priorite", order.getPriority().name());
        orderMap.put("prixTotal", order.getTotalPrice());
        orderMap.put("status", order.getStatus().ordinal());
        orderMap.put("dateCreation", order.getCreationDate());
        orderMap.put("dateModification", order.getModificationDate());
        return orderMap;
    }

    /**
     * Calculate deadline label based on priority
     */
    private String calculateDeadlineLabel(Order order) {
        if (order.getPriority() == Order.OrderPriority.HIGH) {
            return "X"; // Urgent
        } else if (order.getPriority() == Order.OrderPriority.MEDIUM) {
            return "M"; // Medium
        } else {
            return "N"; // Normal
        }
    }

    /**
     * Get recent orders (for OrderController)
     * @return list of recent orders
     */
    @Transactional(readOnly = true)
    public List<Order> getRecentOrders() {
        return orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getCreationDate().compareTo(o1.getCreationDate()))
                .limit(10)
                .collect(Collectors.toList());
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