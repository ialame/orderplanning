package com.pcagrade.planning.entity;

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

    public int getCardCount() {
        return cardCertifications != null ? cardCertifications.size() : 0;
    }
}