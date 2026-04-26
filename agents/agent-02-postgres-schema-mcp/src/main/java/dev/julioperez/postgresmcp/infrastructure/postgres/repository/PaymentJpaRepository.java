package dev.julioperez.postgresmcp.infrastructure.postgres.repository;

import dev.julioperez.postgresmcp.infrastructure.postgres.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
}
