package dev.julioperez.postgresmcp.infrastructure.postgres.repository;

import dev.julioperez.postgresmcp.infrastructure.postgres.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
}
