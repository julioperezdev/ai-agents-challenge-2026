package dev.julioperez.postgresmcp.domain;

import java.util.List;

public record DemoDatasetSummary(
    long customers,
    long products,
    long orders,
    long orderItems,
    long payments,
    List<String> recentCustomers
) {
    public String summaryLine() {
        return "%d clientes, %d productos, %d órdenes, %d ítems y %d pagos."
            .formatted(customers, products, orders, orderItems, payments);
    }
}
