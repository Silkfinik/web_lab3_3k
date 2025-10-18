package org.example.dao.impl

import org.example.dao.api.SubscriberDao
import org.example.db.ConnectionPool
import org.example.entity.Subscriber
import java.sql.Connection
import java.sql.Statement
import org.slf4j.LoggerFactory
import java.sql.SQLException
import org.example.exception.*

class SubscriberDaoImpl : SubscriberDao {

    private val logger = LoggerFactory.getLogger(SubscriberDaoImpl::class.java)

    private companion object {
        const val H2_DUPLICATE_KEY_CODE = 23505

        const val FIND_BY_ID = "SELECT * FROM subscribers WHERE id = ?"
        const val FIND_ALL = "SELECT * FROM subscribers"
        const val BLOCK_SUBSCRIBER = "UPDATE subscribers SET is_blocked = TRUE WHERE id = ?"
        const val ADD_SUBSCRIBER = "INSERT INTO subscribers(name, phone_number) VALUES (?, ?)"
    }

    override fun findById(id: Int): Subscriber? {
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
            connection.prepareStatement(FIND_BY_ID).use { statement ->
                statement.setInt(1, id)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    return Subscriber(
                        id = rs.getInt("id"),
                        name = rs.getString("name"),
                        phoneNumber = rs.getString("phone_number"),
                        balance = rs.getDouble("balance"),
                        isBlocked = rs.getBoolean("is_blocked")
                    )
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to find subscriber by id $id", e)
            throw DataAccessException("Ошибка при поиске абонента.", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
        return null
    }

    override fun findAll(): List<Subscriber> {
        var connection: Connection? = null
        val subscribers = mutableListOf<Subscriber>()
        try {
            connection = ConnectionPool.getConnection()
            connection.prepareStatement(FIND_ALL).use { statement ->
                val rs = statement.executeQuery()
                while (rs.next()) {
                    subscribers.add(
                        Subscriber(
                            id = rs.getInt("id"),
                            name = rs.getString("name"),
                            phoneNumber = rs.getString("phone_number"),
                            balance = rs.getDouble("balance"),
                            isBlocked = rs.getBoolean("is_blocked")
                        )
                    )
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to find all subscribers", e)
            throw DataAccessException("Ошибка при получении списка абонентов.", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
        return subscribers
    }

    override fun block(subscriberId: Int) {
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
            connection.prepareStatement(BLOCK_SUBSCRIBER).use { statement ->
                statement.setInt(1, subscriberId)
                statement.executeUpdate()
                logger.info("Subscriber $subscriberId was blocked.")
            }
        } catch (e: SQLException) {
            logger.error("Failed to block subscriber $subscriberId", e)
            throw DataAccessException("Ошибка при блокировке абонента.", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
    }

    override fun add(subscriber: Subscriber): Subscriber {
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
            connection.prepareStatement(ADD_SUBSCRIBER, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.setString(1, subscriber.name)
                statement.setString(2, subscriber.phoneNumber)
                statement.executeUpdate()

                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    val id = generatedKeys.getInt(1)
                    logger.info("New subscriber created with id $id")
                    return subscriber.copy(id = id)
                } else {
                    throw DataAccessException("Не удалось создать абонента, не получен ID.")
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to add subscriber $subscriber", e)
            if (e.errorCode == H2_DUPLICATE_KEY_CODE) {
                throw DuplicateEntryException("Абонент с номером ${subscriber.phoneNumber} уже существует.", e)
            }
            throw DataAccessException("Ошибка при добавлении абонента.", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
    }
}