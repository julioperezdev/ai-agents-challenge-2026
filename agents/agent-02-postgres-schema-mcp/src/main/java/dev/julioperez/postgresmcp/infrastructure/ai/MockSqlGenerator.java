package dev.julioperez.postgresmcp.infrastructure.ai;

import dev.julioperez.postgresmcp.domain.port.NaturalLanguageSqlGenerator;

import java.util.Locale;

public class MockSqlGenerator implements NaturalLanguageSqlGenerator {

    @Override
    public String generateSql(String question, String schemaDocumentation, int limit) {
        String normalized = question.toLowerCase(Locale.ROOT).trim();

        if (normalized.contains("listar clientes") || normalized.contains("clientes más recientes")) {
            return """
                SELECT id, full_name, email, created_at
                FROM customers
                ORDER BY created_at DESC
                LIMIT %d
                """.formatted(limit);
        }

        if (normalized.contains("órdenes pagadas") || normalized.contains("ordenes pagadas")) {
            return """
                SELECT o.id, c.full_name, o.order_date, o.status, o.total_amount
                FROM orders o
                JOIN customers c ON c.id = o.customer_id
                WHERE o.status = 'PAID'
                ORDER BY o.order_date DESC
                LIMIT %d
                """.formatted(limit);
        }

        if (normalized.contains("productos más vendidos") || normalized.contains("productos mas vendidos")) {
            return """
                SELECT p.id, p.name, SUM(oi.quantity) AS total_units_sold
                FROM order_items oi
                JOIN products p ON p.id = oi.product_id
                GROUP BY p.id, p.name
                ORDER BY total_units_sold DESC, p.name ASC
                LIMIT %d
                """.formatted(limit);
        }

        if (normalized.contains("ventas por cliente")
            || normalized.contains("monto pagado")
            || normalized.contains("pagos por cliente")
            || normalized.contains("dinero se ha aprobado")) {
            return """
                SELECT c.id, c.full_name, SUM(p.amount) AS total_paid
                FROM customers c
                JOIN orders o ON o.customer_id = c.id
                JOIN payments p ON p.order_id = o.id
                WHERE p.status = 'APPROVED'
                GROUP BY c.id, c.full_name
                ORDER BY total_paid DESC
                LIMIT %d
                """.formatted(limit);
        }

        if (normalized.contains("órdenes canceladas") || normalized.contains("ordenes canceladas")) {
            return """
                SELECT o.id, c.full_name, o.order_date, o.total_amount
                FROM orders o
                JOIN customers c ON c.id = o.customer_id
                WHERE o.status = 'CANCELLED'
                ORDER BY o.order_date DESC
                LIMIT %d
                """.formatted(limit);
        }

        throw new IllegalArgumentException(
            "El modo mock solo soporta algunas preguntas demo. Prueba con: listar clientes, órdenes pagadas, "
                + "productos más vendidos, ventas por cliente u órdenes canceladas."
        );
    }
}
