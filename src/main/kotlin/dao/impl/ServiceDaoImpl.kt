package org.example.dao.impl

import org.example.dao.api.ServiceDao
import org.example.db.ConnectionPool
import org.example.entity.Service
import java.sql.Connection
import org.slf4j.LoggerFactory
import java.sql.SQLException
import org.example.exception.*

class ServiceDaoImpl : ServiceDao {

    private val logger = LoggerFactory.getLogger(ServiceDaoImpl::class.java)

    private companion object {
        const val H2_DUPLICATE_KEY_CODE = 23505
        const val H2_INTEGRITY_VIOLATION_CODE = 23503

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
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
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
        } catch (e: SQLException) {
            logger.error("Failed to find all services", e)
            throw DataAccessException("Ошибка при получении списка услуг", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
        return services
    }

    override fun findBySubscriberId(subscriberId: Int): List<Service> {
        val services = mutableListOf<Service>()
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
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
        } catch (e: SQLException) {
            logger.error("Failed to find services for subscriber $subscriberId", e)
            throw DataAccessException("Ошибка при поиске услуг абонента.", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
        return services
    }

    override fun linkServiceToSubscriber(subscriberId: Int, serviceId: Int) {
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
            connection.prepareStatement(LINK_SERVICE).use { statement ->
                statement.setInt(1, subscriberId)
                statement.setInt(2, serviceId)
                statement.executeUpdate()
                logger.info("Service $serviceId linked to subscriber $subscriberId")
            }
        } catch (e: SQLException) {
            logger.error("Failed to link service $serviceId to subscriber $subscriberId", e)
            when (e.errorCode) {
                H2_DUPLICATE_KEY_CODE ->
                    throw DuplicateEntryException("Эта услуга уже подключена абоненту.", e)
                H2_INTEGRITY_VIOLATION_CODE ->
                    throw EntryNotFoundException("Абонент (ID $subscriberId) или услуга (ID $serviceId) не найдены.", e)
                else ->
                    throw DataAccessException("Ошибка при подключении услуги.", e)
            }
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
    }
}