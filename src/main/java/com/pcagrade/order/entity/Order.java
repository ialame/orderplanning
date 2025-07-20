package com.pcagrade.order.entity;

import com.pcagrade.order.util.AbstractUlidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "`order`")
public class Order extends AbstractUlidEntity {
    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "card_count")
    private Integer cardCount;

    @Column(name = "priority_string")
    private String priority;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "modification_date")
    private LocalDateTime modificationDate;

    @Column(name = "deadline_date")
    private LocalDateTime deadlineDate;

    @Column(name = "processing_start_date")
    private LocalDateTime processingStartDate;

    @Column(name = "processing_end_date")
    private LocalDateTime processingEndDate;

    @Column(name = "unseal_count")
    private Integer unsealCount;

    // Methods
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

//    public int getCardCount() {
//        return cardCertifications != null ? cardCertifications.size() : 0;
//    }


    public String getOrderNumber() {
        return orderNumber;
    }

    public Integer getCardCount() {
        return cardCount;
    }

    public void setCardCount(Integer cardCount) {
        this.cardCount = cardCount;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public LocalDateTime getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(LocalDateTime deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public LocalDateTime getProcessingStartDate() {
        return processingStartDate;
    }

    public void setProcessingStartDate(LocalDateTime processingStartDate) {
        this.processingStartDate = processingStartDate;
    }

    public LocalDateTime getProcessingEndDate() {
        return processingEndDate;
    }

    public void setProcessingEndDate(LocalDateTime processingEndDate) {
        this.processingEndDate = processingEndDate;
    }

    public Integer getUnsealCount() {
        return unsealCount;
    }

    public void setUnsealCount(Integer unsealCount) {
        this.unsealCount = unsealCount;
    }
}