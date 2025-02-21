package com.example.batchexample.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@Table(name = "payment_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRequest {
    @Id
    @Column(name="request_id")
    private UUID requestId;
    private UUID requestMemberId;
    private BigDecimal requestPrice;
    private UUID sellerId;
    private int paymentStatus;
    private Instant approvedAt;
    private Instant createdAt;
}
