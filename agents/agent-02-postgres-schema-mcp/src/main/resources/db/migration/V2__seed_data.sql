INSERT INTO customers (id, full_name, email, city, created_at) VALUES
(1, 'Ana Torres', 'ana.torres@test.com', 'Buenos Aires', '2026-01-05 10:00:00'),
(2, 'Carlos Rivas', 'carlos.rivas@test.com', 'Cordoba', '2026-01-09 11:15:00'),
(3, 'Luis Mendoza', 'luis.mendoza@test.com', 'Rosario', '2026-01-14 09:30:00'),
(4, 'Sofia Perez', 'sofia.perez@test.com', 'Mendoza', '2026-01-18 16:45:00'),
(5, 'Mariana Diaz', 'mariana.diaz@test.com', 'La Plata', '2026-01-26 13:10:00'),
(6, 'Pedro Gomez', 'pedro.gomez@test.com', 'Salta', '2026-02-03 08:20:00'),
(7, 'Valentina Ruiz', 'valentina.ruiz@test.com', 'Mar del Plata', '2026-02-11 17:05:00'),
(8, 'Diego Castro', 'diego.castro@test.com', 'Neuquen', '2026-02-19 12:00:00');

INSERT INTO products (id, name, category, price, active, created_at) VALUES
(1, 'Laptop Pro 14', 'Computers', 1200.00, TRUE, '2026-01-01 09:00:00'),
(2, 'Wireless Mouse', 'Accessories', 25.00, TRUE, '2026-01-01 09:00:00'),
(3, 'Mechanical Keyboard', 'Accessories', 55.00, TRUE, '2026-01-01 09:00:00'),
(4, '4K Monitor', 'Monitors', 300.00, TRUE, '2026-01-01 09:00:00'),
(5, 'Noise Cancelling Headphones', 'Audio', 80.00, TRUE, '2026-01-01 09:00:00'),
(6, 'HD Webcam', 'Video', 60.00, TRUE, '2026-01-01 09:00:00'),
(7, 'Ergonomic Chair', 'Furniture', 250.00, TRUE, '2026-01-01 09:00:00'),
(8, 'Standing Desk', 'Furniture', 400.00, TRUE, '2026-01-01 09:00:00'),
(9, 'USB-C Hub', 'Accessories', 45.00, TRUE, '2026-01-01 09:00:00'),
(10, 'Portable SSD 1TB', 'Storage', 150.00, TRUE, '2026-01-01 09:00:00');

INSERT INTO orders (id, customer_id, order_date, status, total_amount) VALUES
(1, 1, '2026-03-01', 'PAID', 1280.00),
(2, 2, '2026-03-02', 'SHIPPED', 355.00),
(3, 3, '2026-03-03', 'CANCELLED', 150.00),
(4, 1, '2026-03-05', 'PAID', 105.00),
(5, 4, '2026-03-07', 'CREATED', 445.00),
(6, 5, '2026-03-08', 'PAID', 460.00),
(7, 6, '2026-03-10', 'SHIPPED', 850.00),
(8, 7, '2026-03-12', 'PAID', 70.00),
(9, 8, '2026-03-14', 'CANCELLED', 250.00),
(10, 2, '2026-03-15', 'PAID', 505.00),
(11, 3, '2026-03-17', 'SHIPPED', 325.00),
(12, 5, '2026-03-19', 'PAID', 200.00),
(13, 4, '2026-03-20', 'CREATED', 1200.00),
(14, 6, '2026-03-22', 'PAID', 480.00),
(15, 7, '2026-03-24', 'SHIPPED', 390.00);

INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, line_total) VALUES
(1, 1, 1, 1, 1200.00, 1200.00),
(2, 1, 5, 1, 80.00, 80.00),
(3, 2, 4, 1, 300.00, 300.00),
(4, 2, 3, 1, 55.00, 55.00),
(5, 3, 10, 1, 150.00, 150.00),
(6, 4, 2, 2, 25.00, 50.00),
(7, 4, 9, 1, 45.00, 45.00),
(8, 4, 6, 1, 60.00, 60.00),
(9, 5, 8, 1, 400.00, 400.00),
(10, 5, 9, 1, 45.00, 45.00),
(11, 6, 7, 1, 250.00, 250.00),
(12, 6, 10, 1, 150.00, 150.00),
(13, 6, 2, 2, 25.00, 50.00),
(14, 6, 3, 1, 10.00, 10.00),
(15, 7, 8, 1, 400.00, 400.00),
(16, 7, 7, 1, 250.00, 250.00),
(17, 7, 5, 2, 80.00, 160.00),
(18, 7, 6, 1, 40.00, 40.00),
(19, 8, 2, 1, 25.00, 25.00),
(20, 8, 9, 1, 45.00, 45.00),
(21, 9, 7, 1, 250.00, 250.00),
(22, 10, 4, 1, 300.00, 300.00),
(23, 10, 10, 1, 150.00, 150.00),
(24, 10, 9, 1, 45.00, 45.00),
(25, 10, 2, 2, 5.00, 10.00),
(26, 11, 4, 1, 300.00, 300.00),
(27, 11, 2, 1, 25.00, 25.00),
(28, 12, 10, 1, 150.00, 150.00),
(29, 12, 2, 2, 25.00, 50.00),
(30, 13, 1, 1, 1200.00, 1200.00),
(31, 14, 8, 1, 400.00, 400.00),
(32, 14, 2, 1, 25.00, 25.00),
(33, 14, 9, 1, 45.00, 45.00),
(34, 14, 3, 1, 10.00, 10.00),
(35, 15, 7, 1, 250.00, 250.00),
(36, 15, 6, 1, 60.00, 60.00),
(37, 15, 5, 1, 80.00, 80.00);

INSERT INTO payments (id, order_id, status, amount, provider, reference_code, payment_date) VALUES
(1, 1, 'APPROVED', 1280.00, 'stripe', 'pay_0001', '2026-03-01 11:00:00'),
(2, 2, 'APPROVED', 355.00, 'mercadopago', 'pay_0002', '2026-03-02 15:00:00'),
(3, 3, 'REFUNDED', 150.00, 'stripe', 'pay_0003', '2026-03-03 18:10:00'),
(4, 4, 'APPROVED', 105.00, 'paypal', 'pay_0004', '2026-03-05 12:30:00'),
(5, 5, 'PENDING', 445.00, 'stripe', 'pay_0005', '2026-03-07 14:00:00'),
(6, 6, 'APPROVED', 460.00, 'mercadopago', 'pay_0006', '2026-03-08 09:20:00'),
(7, 7, 'APPROVED', 850.00, 'stripe', 'pay_0007', '2026-03-10 10:15:00'),
(8, 8, 'APPROVED', 70.00, 'paypal', 'pay_0008', '2026-03-12 08:45:00'),
(9, 9, 'REJECTED', 250.00, 'stripe', 'pay_0009', '2026-03-14 17:50:00'),
(10, 10, 'APPROVED', 505.00, 'mercadopago', 'pay_0010', '2026-03-15 13:40:00'),
(11, 11, 'APPROVED', 325.00, 'stripe', 'pay_0011', '2026-03-17 11:05:00'),
(12, 12, 'APPROVED', 200.00, 'paypal', 'pay_0012', '2026-03-19 16:30:00');

SELECT setval('customers_id_seq', 8, true);
SELECT setval('products_id_seq', 10, true);
SELECT setval('orders_id_seq', 15, true);
SELECT setval('order_items_id_seq', 37, true);
SELECT setval('payments_id_seq', 12, true);
