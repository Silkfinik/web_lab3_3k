package org.example.dao.impl

import org.example.dao.api.SubscriberDao
import org.example.db.JdbcConnector
import org.example.entity.Subscriber
import java.sql.SQLException
import java.sql.Statement

class SubscriberDaoImpl : SubscriberDao {

    private companion object {
        const val FIND_BY_ID = "SELECT * FROM subscribers WHERE id = ?"
        const val FIND_ALL = "SELECT * FROM subscribers"
        const val BLOCK_SUBSCRIBER = "UPDATE subscribers SET is_blocked = TRUE WHERE id = ?"
        const val ADD_SUBSCRIBER = "INSERT INTO subscribers(name, phone_number) VALUES (?, ?)"
    }

    override fun findById(id: Int): Subscriber? {
        JdbcConnector.getConnection().use { connection ->
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
        }
        return null
    }

    override fun findAll(): List<Subscriber> {
        val subscribers = mutableListOf<Subscriber>()
        JdbcConnector.getConnection().use { connection ->
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
        }
        return subscribers
    }

    override fun block(subscriberId: Int) {
        JdbcConnector.getConnection().use { connection ->
            connection.prepareStatement(BLOCK_SUBSCRIBER).use { statement ->
                statement.setInt(1, subscriberId)
                statement.executeUpdate()
            }
        }
    }

    override fun add(subscriber: Subscriber): Subscriber {
        JdbcConnector.getConnection().use { connection ->
            connection.prepareStatement(ADD_SUBSCRIBER, Statement.RETURN_GENERATED_KEYS).use { statement ->
                statement.setString(1, subscriber.name)
                statement.setString(2, subscriber.phoneNumber)
                statement.executeUpdate()

                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    val id = generatedKeys.getInt(1)
                    return subscriber.copy(id = id)
                } else {
                    throw SQLException("Creating subscriber failed, no ID obtained.")
                }
            }
        }
    }
}