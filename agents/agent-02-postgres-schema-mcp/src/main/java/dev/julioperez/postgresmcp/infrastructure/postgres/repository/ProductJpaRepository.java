package dev.julioperez.postgresmcp.infrastructure.postgres.repository;

import dev.julioperez.postgresmcp.infrastructure.postgres.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
}
