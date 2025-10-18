package org.example.dao.impl

import org.example.dao.api.ServiceDao
import org.example.db.JdbcConnector
import org.example.entity.Service

class ServiceDaoImpl : ServiceDao {

    private companion object {
        const val FIND_ALL = "SELECT * FROM services"
        const val FIND_BY_SUBSCRIBER_ID = """
            SELECT s.id, s.name, s.monthly_fee 
            FROM services s
            JOIN subscriber_services ss ON s.id = ss.service_id
            WHERE ss.subscriber_id = ?
        """
        const val LINK_SERVICE = "INSERT INTO subscriber_services(subscriber_id, service_id) VALUES (?, ?)"
    }

    override fun findAll(): List<Service> {
        val services = mutableListOf<Service>()
        JdbcConnector.getConnection().use { connection ->
            connection.prepareStatement(FIND_ALL).use { statement ->
                val rs = statement.executeQuery()
                while (rs.next()) {
                    services.add(
                        Service(
                            id = rs.getInt("id"),
                            name = rs.getString("name"),
                            monthlyFee = rs.getDouble("monthly_fee")
                        )
                    )
                }
            }
        }
        return services
    }

    override fun findBySubscriberId(subscriberId: Int): List<Service> {
        val services = mutableListOf<Service>()
        JdbcConnector.getConnection().use { connection ->
            connection.prepareStatement(FIND_BY_SUBSCRIBER_ID).use { statement ->
                statement.setInt(1, subscriberId)
                val rs = statement.executeQuery()
                while (rs.next()) {
                    services.add(
                        Service(
                            id = rs.getInt("id"),
                            name = rs.getString("name"),
                            monthlyFee = rs.getDouble("monthly_fee")
                        )
                    )
                }
            }
        }
        return services
    }

    override fun linkServiceToSubscriber(subscriberId: Int, serviceId: Int) {
        JdbcConnector.getConnection().use { connection ->
            connection.prepareStatement(LINK_SERVICE).use { statement ->
                statement.setInt(1, subscriberId)
                statement.setInt(2, serviceId)
                statement.executeUpdate()
            }
        }
    }
}