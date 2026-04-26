package dev.julioperez.postgresmcp.infrastructure.postgres.repository;

import dev.julioperez.postgresmcp.infrastructure.postgres.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {
}
