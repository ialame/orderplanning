// ========== FICHIER 2: OrderPriority.java ==========
package com.pcagrade.order.entity;

/**
 * Order priority enumeration
 * Translated from PrioriteCommande
 */
public enum OrderPriority {
    HIGH,     // HAUTE - 1 week - price >= 1000€
    MEDIUM,   // MOYENNE - 2 weeks - price >= 500€
    LOW       // BASSE - 4 weeks - price < 500€
}