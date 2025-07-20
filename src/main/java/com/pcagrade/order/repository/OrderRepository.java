package com.pcagrade.order.repository;

import com.pcagrade.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Order entity management
 * Translated from CommandeRepository to OrderRepository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find order by order number
     * @param orderNumber the order number to search for
     * @return optional order
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by status
     * @param status the status code to filter by
     * @return list of orders with specified status
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findOrdersByStatus(@Param("status") int status);

    /**
     * Find unassigned orders (status 1 = pending)
     * @param status the status code (typically 1 for pending)
     * @return list of unassigned orders
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findUnassignedOrders(@Param("status") int status);

    /**
     * Count orders by status
     * @param status the status to count
     * @return number of orders with this status
     */
    long countByStatus(int status);

    /**
     * Find orders that need processing (status 1 or 2)
     * @return list of orders to be processed, ordered by date
     */
    @Query("SELECT o FROM Order o WHERE o.status IN (1, 2) ORDER BY o.orderDate ASC")
    List<Order> findOrdersToProcess();

    /**
     * Find orders created after specific date
     * @param date the date after which to search
     * @return list of orders created after the specified date
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :date ORDER BY o.orderDate ASC")
    List<Order> findOrdersAfterDate(@Param("date") LocalDate date);

    /**
     * Find orders by priority
     * @param priority the priority level
     * @return list of orders with specified priority
     */
    List<Order> findByPriorityOrderByOrderDateAsc(String priority);

    /**
     * Find orders between dates
     * @param startDate start date
     * @param endDate end date
     * @return list of orders between specified dates
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate ASC")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find pending orders (status = 1)
     * @return list of pending orders
     */
    @Query("SELECT o FROM Order o WHERE o.status = 1 ORDER BY o.priority DESC, o.orderDate ASC")
    List<Order> findPendingOrders();

    /**
     * Find in progress orders (status = 2)
     * @return list of in progress orders
     */
    @Query("SELECT o FROM Order o WHERE o.status = 2 ORDER BY o.orderDate ASC")
    List<Order> findInProgressOrders();

    /**
     * Find completed orders (status = 3)
     * @return list of completed orders
     */
    @Query("SELECT o FROM Order o WHERE o.status = 3 ORDER BY o.orderDate DESC")
    List<Order> findCompletedOrders();

    /**
     * Find orders by card count range
     * @param minCards minimum number of cards
     * @param maxCards maximum number of cards
     * @return list of orders within card count range
     */
    @Query("SELECT o FROM Order o WHERE o.cardCount BETWEEN :minCards AND :maxCards ORDER BY o.cardCount ASC")
    List<Order> findOrdersByCardCountRange(@Param("minCards") int minCards, @Param("maxCards") int maxCards);

    /**
     * Get total card count for all orders
     * @return sum of all cards across all orders
     */
    @Query("SELECT COALESCE(SUM(o.cardCount), 0) FROM Order o")
    Long getTotalCardCount();

    /**
     * Get average processing time in minutes
     * @return average processing time
     */
    @Query("SELECT AVG(o.estimatedTimeMinutes) FROM Order o WHERE o.estimatedTimeMinutes > 0")
    Double getAverageProcessingTime();

    /**
     * Find orders with deadlines approaching (within next N days)
     * @param days number of days to look ahead
     * @return list of orders with approaching deadlines
     */
    @Query("SELECT o FROM Order o WHERE o.deadline BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, :days) ORDER BY o.deadline ASC")
    List<Order> findOrdersWithApproachingDeadlines(@Param("days") int days);
}