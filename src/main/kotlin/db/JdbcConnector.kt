package org.example.db

import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

object JdbcConnector {

    private val config = Properties()
    private val dbUrl: String

    init {
        val inputStream = javaClass.classLoader.getResourceAsStream("database.properties")
            ?: throw IllegalStateException("database.properties not found in classpath")
        config.load(inputStream)
        dbUrl = config.getProperty("db.host")
    }

    /**
     * Предоставляет готовое к использованию соединение с базой данных.
     */
    fun getConnection(): Connection {
        val connectionProps = Properties()
        connectionProps.setProperty("user", config.getProperty("db.user"))
        connectionProps.setProperty("password", config.getProperty("db.password"))

        return DriverManager.getConnection(dbUrl, connectionProps)
    }
}