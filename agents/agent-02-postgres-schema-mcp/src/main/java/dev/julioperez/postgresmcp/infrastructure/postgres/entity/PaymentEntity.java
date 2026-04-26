package dev.julioperez.postgresmcp.infrastructure.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 40)
    private String provider;

    @Column(name = "reference_code", nullable = false, length = 64)
    private String referenceCode;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    public Long getId() {
        return id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getProvider() {
        return provider;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
}
