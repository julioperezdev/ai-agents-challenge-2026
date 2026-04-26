package dev.julioperez.postgresmcp.infrastructure.postgres.repository;

import dev.julioperez.postgresmcp.infrastructure.postgres.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {

    List<CustomerEntity> findTop3ByOrderByCreatedAtDesc();
}
