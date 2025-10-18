package org.example.db

import java.sql.DriverManager
import java.util.Properties

object DatabaseManager {

    private val properties = Properties()

    private const val CREATE_SUBSCRIBERS_TABLE = """
        CREATE TABLE IF NOT EXISTS subscribers (
            id INT PRIMARY KEY AUTO_INCREMENT,
            name VARCHAR(255) NOT NULL,
            phone_number VARCHAR(20) UNIQUE NOT NULL,
            balance DECIMAL(10, 2) DEFAULT 0.00,
            is_blocked BOOLEAN DEFAULT FALSE
        )
    """

    private const val CREATE_SERVICES_TABLE = """
        CREATE TABLE IF NOT EXISTS services (
            id INT PRIMARY KEY AUTO_INCREMENT,
            name VARCHAR(255) UNIQUE NOT NULL,
            monthly_fee DECIMAL(10, 2) NOT NULL
        )
    """

    private const val CREATE_SUBSCRIBER_SERVICES_TABLE = """
        CREATE TABLE IF NOT EXISTS subscriber_services (
            subscriber_id INT,
            service_id INT,
            PRIMARY KEY (subscriber_id, service_id),
            FOREIGN KEY (subscriber_id) REFERENCES subscribers(id) ON DELETE CASCADE,
            FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
        )
    """

    private const val CREATE_INVOICES_TABLE = """
        CREATE TABLE IF NOT EXISTS invoices (
            id INT PRIMARY KEY AUTO_INCREMENT,
            subscriber_id INT,
            amount DECIMAL(10, 2) NOT NULL,
            issue_date DATE NOT NULL,
            is_paid BOOLEAN DEFAULT FALSE,
            FOREIGN KEY (subscriber_id) REFERENCES subscribers(id) ON DELETE CASCADE
        )
    """

    init {
        val inputStream = javaClass.classLoader.getResourceAsStream("database.properties")
        properties.load(inputStream)
    }

    fun initDatabase() {
        val dbUrl = properties.getProperty("db.host")
        val dbUser = properties.getProperty("db.user")
        val dbPassword = properties.getProperty("db.password")

        DriverManager.getConnection(dbUrl, dbUser, dbPassword).use { connection ->
            connection.createStatement().use { statement ->
                println("Creating tables in H2 database...")
                statement.executeUpdate(CREATE_SUBSCRIBERS_TABLE)
                statement.executeUpdate(CREATE_SERVICES_TABLE)
                statement.executeUpdate(CREATE_SUBSCRIBER_SERVICES_TABLE)
                statement.executeUpdate(CREATE_INVOICES_TABLE)
                println("Tables created successfully.")
            }
        }
    }
}