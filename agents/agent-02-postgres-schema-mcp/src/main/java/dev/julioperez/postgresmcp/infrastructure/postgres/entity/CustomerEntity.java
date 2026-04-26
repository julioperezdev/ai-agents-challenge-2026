package dev.julioperez.postgresmcp.infrastructure.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "customer", fetch = LAZY)
    private Set<OrderEntity> orders = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getCity() {
        return city;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<OrderEntity> getOrders() {
        return orders;
    }
}
