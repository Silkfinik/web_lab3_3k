package org.example.db

import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

object ConnectionPool {

    private val logger = LoggerFactory.getLogger(ConnectionPool::class.java)

    private const val POOL_SIZE = 10

    private val connectionQueue: BlockingQueue<Connection>

    private val config = Properties()
    private val dbUrl: String

    init {
        logger.info("Initializing Connection Pool...")
        try {
            val inputStream = javaClass.classLoader.getResourceAsStream("database.properties")
                ?: throw IllegalStateException("database.properties not found")
            config.load(inputStream)
            dbUrl = config.getProperty("db.host")

            connectionQueue = ArrayBlockingQueue(POOL_SIZE)

            for (i in 1..POOL_SIZE) {
                val connection = createConnection()
                connectionQueue.put(connection)
            }

            logger.info("Connection Pool initialized successfully with $POOL_SIZE connections.")
        } catch (e: Exception) {
            logger.error("Failed to initialize Connection Pool!", e)
            throw e
        }
    }

    /**
     * Создает одно физическое соединение с БД
     */
    private fun createConnection(): Connection {
        val connectionProps = Properties()
        connectionProps.setProperty("user", config.getProperty("db.user"))
        connectionProps.setProperty("password", config.getProperty("db.password"))
        return DriverManager.getConnection(dbUrl, connectionProps)
    }

    /**
     * Взять соединение из пула.
     * Этот метод будет ждать, пока соединение не освободится.
     */
    fun getConnection(): Connection {
        logger.debug("Waiting to get connection from pool...")
        val connection = connectionQueue.take()
        logger.debug("Got connection from pool. Remaining: ${connectionQueue.size}")
        return connection
    }

    /**
     * Вернуть соединение обратно в пул.
     */
    fun releaseConnection(connection: Connection?) {
        if (connection != null) {
            try {
                connectionQueue.put(connection)
                logger.debug("Connection released back to pool. Pool size: ${connectionQueue.size}")
            } catch (e: InterruptedException) {
                logger.error("Failed to release connection back to pool", e)
            }
        }
    }
}