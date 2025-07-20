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

    @GetMapping("/frontend/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrdersFrontend() {
        try {
            System.out.println("📋 Frontend: Retrieving orders with real data");

            List<Map<String, Object>> orders = orderService.getRecentOrders();

            System.out.println("✅ " + orders.size() + " orders returned");
            return ResponseEntity.ok(orders);

        } catch (Exception e) {
            System.err.println("❌ Error retrieving orders: " + e.getMessage());
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

        } catch (Exception e) {
            System.err.println("❌ Error loading cards: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}