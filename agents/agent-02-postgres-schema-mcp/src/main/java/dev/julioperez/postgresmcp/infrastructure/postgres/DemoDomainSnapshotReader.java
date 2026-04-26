package dev.julioperez.postgresmcp.infrastructure.postgres;

import dev.julioperez.postgresmcp.domain.DemoDatasetSummary;
import dev.julioperez.postgresmcp.domain.port.DemoDatasetSummaryRepository;
import dev.julioperez.postgresmcp.infrastructure.postgres.repository.CustomerJpaRepository;
import dev.julioperez.postgresmcp.infrastructure.postgres.repository.OrderItemJpaRepository;
import dev.julioperez.postgresmcp.infrastructure.postgres.repository.OrderJpaRepository;
import dev.julioperez.postgresmcp.infrastructure.postgres.repository.PaymentJpaRepository;
import dev.julioperez.postgresmcp.infrastructure.postgres.repository.ProductJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DemoDomainSnapshotReader implements DemoDatasetSummaryRepository {

    private final CustomerJpaRepository customerJpaRepository;
    private final ProductJpaRepository productJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final PaymentJpaRepository paymentJpaRepository;

    public DemoDomainSnapshotReader(
        CustomerJpaRepository customerJpaRepository,
        ProductJpaRepository productJpaRepository,
        OrderJpaRepository orderJpaRepository,
        OrderItemJpaRepository orderItemJpaRepository,
        PaymentJpaRepository paymentJpaRepository
    ) {
        this.customerJpaRepository = customerJpaRepository;
        this.productJpaRepository = productJpaRepository;
        this.orderJpaRepository = orderJpaRepository;
        this.orderItemJpaRepository = orderItemJpaRepository;
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public DemoDatasetSummary getSummary() {
        return new DemoDatasetSummary(
            customerJpaRepository.count(),
            productJpaRepository.count(),
            orderJpaRepository.count(),
            orderItemJpaRepository.count(),
            paymentJpaRepository.count(),
            customerJpaRepository.findTop3ByOrderByCreatedAtDesc().stream()
                .map(customer -> customer.getFullName())
                .toList()
        );
    }

}
