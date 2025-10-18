package org.example.db

object DataInitializer {
    fun insertInitialData() {
        println("Inserting initial data for testing...")
        val sqlStatements = listOf(
            // Абоненты
            "DELETE FROM subscribers;",
            "INSERT INTO subscribers(name, phone_number, balance, is_blocked) VALUES ('Иван Иванов', '+375291234567', 150.50, false);",
            "INSERT INTO subscribers(name, phone_number, balance, is_blocked) VALUES ('Петр Петров', '+375337654321', -50.00, true);",

            // Услуги
            "DELETE FROM services;",
            "INSERT INTO services(id, name, monthly_fee) VALUES (101, 'Интернет 50 Мбит/с', 450.00);",
            "INSERT INTO services(id, name, monthly_fee) VALUES (102, 'Мобильная связь', 300.00);",
            "INSERT INTO services(id, name, monthly_fee) VALUES (103, 'Антивирус', 100.00);",

            // Подключенные услуги
            "DELETE FROM subscriber_services;",
            "INSERT INTO subscriber_services(subscriber_id, service_id) VALUES (1, 101);",
            "INSERT INTO subscriber_services(subscriber_id, service_id) VALUES (1, 102);",
            "INSERT INTO subscriber_services(subscriber_id, service_id) VALUES (2, 102);",
            "INSERT INTO subscriber_services(subscriber_id, service_id) VALUES (2, 103);",

            // Счета
            "DELETE FROM invoices;",
            "INSERT INTO invoices(id, subscriber_id, amount, issue_date, is_paid) VALUES (1001, 1, 750.00, '2025-09-01', true);",
            "INSERT INTO invoices(id, subscriber_id, amount, issue_date, is_paid) VALUES (1002, 2, 400.00, '2025-09-05', false);"
        )

        JdbcConnector.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                sqlStatements.forEach { sql ->
                    statement.executeUpdate(sql)
                }
            }
        }
        println("Initial data inserted.")
    }
}