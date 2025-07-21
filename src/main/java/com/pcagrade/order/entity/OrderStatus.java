// ========== FICHIER 1: OrderStatus.java ==========
package com.pcagrade.order.entity;

/**
 * Order status enumeration
 * Translated from StatutCommande
 */
public enum OrderStatus {
    PENDING,      // EN_ATTENTE - Waiting to be processed
    SCHEDULED,    // PLANIFIEE - Scheduled for processing  
    IN_PROGRESS,  // EN_COURS - Currently being processed
    COMPLETED,    // TERMINEE - Processing completed
    CANCELLED     // ANNULEE - Order cancelled
}