package org.example.dao.impl

import org.example.dao.api.InvoiceDao
import org.example.db.ConnectionPool
import org.example.entity.Invoice
import java.sql.Connection
import org.slf4j.LoggerFactory
import java.sql.SQLException
import org.example.exception.*

class InvoiceDaoImpl : InvoiceDao {

    private val logger = LoggerFactory.getLogger(InvoiceDaoImpl::class.java)

    private companion object {
        const val H2_DUPLICATE_KEY_CODE = 23505
        const val H2_INTEGRITY_VIOLATION_CODE = 23503

        const val FIND_BY_SUBSCRIBER_ID = "SELECT * FROM invoices WHERE subscriber_id = ?"
        const val PAY_INVOICE = "UPDATE invoices SET is_paid = TRUE WHERE id = ?"
        const val FIND_SUBSCRIBER_ID_BY_INVOICE_ID = "SELECT subscriber_id FROM invoices WHERE id = ?"
        const val FIND_UNPAID = "SELECT * FROM invoices WHERE is_paid = FALSE"
    }

    override fun findBySubscriberId(subscriberId: Int): List<Invoice> {
        val invoices = mutableListOf<Invoice>()
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
            connection.prepareStatement(FIND_BY_SUBSCRIBER_ID).use { statement ->
                statement.setInt(1, subscriberId)
                val rs = statement.executeQuery()
                while (rs.next()) {
                    invoices.add(
                        Invoice(
                            id = rs.getInt("id"),
                            subscriberId = rs.getInt("subscriber_id"),
                            amount = rs.getDouble("amount"),
                            issueDate = rs.getDate("issue_date").toLocalDate(),
                            isPaid = rs.getBoolean("is_paid")
                        )
                    )
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to find invoices for subscriber $subscriberId", e)
            throw DataAccessException("Ошибка при поиске счетов абонента.", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
        return invoices
    }

    override fun pay(invoiceId: Int): Boolean {
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
            connection.prepareStatement(PAY_INVOICE).use { statement ->
                statement.setInt(1, invoiceId)
                val rowsAffected = statement.executeUpdate()

                if (rowsAffected == 0) {
                    logger.warn("Payment failed. Invoice with id $invoiceId not found.")
                    throw EntryNotFoundException("Счет с ID $invoiceId не найден.")
                }

                logger.info("Invoice $invoiceId was paid successfully.")
                return true
            }
        } catch (e: SQLException) {
            logger.error("Failed to pay invoice $invoiceId", e)
            throw DataAccessException("Ошибка при оплате счета.", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
    }

    override fun findSubscriberIdByInvoiceId(invoiceId: Int): Int? {
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
            connection.prepareStatement(FIND_SUBSCRIBER_ID_BY_INVOICE_ID).use { statement ->
                statement.setInt(1, invoiceId)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    return rs.getInt("subscriber_id")
                } else {
                    logger.warn("Subscriber ID not found for invoice $invoiceId.")
                    throw EntryNotFoundException("Счет с ID $invoiceId не найден.", null)
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to find subscriber by invoice $invoiceId", e)
            throw DataAccessException("Ошибка при поиске счета.", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
    }

    override fun findUnpaid(): List<Invoice> {
        val invoices = mutableListOf<Invoice>()
        var connection: Connection? = null
        try {
            connection = ConnectionPool.getConnection()
            connection.prepareStatement(FIND_UNPAID).use { statement ->
                val rs = statement.executeQuery()
                while (rs.next()) {
                    invoices.add(
                        Invoice(
                            id = rs.getInt("id"),
                            subscriberId = rs.getInt("subscriber_id"),
                            amount = rs.getDouble("amount"),
                            issueDate = rs.getDate("issue_date").toLocalDate(),
                            isPaid = rs.getBoolean("is_paid")
                        )
                    )
                }
            }
        } catch (e: SQLException) {
            logger.error("Failed to find unpaid invoices", e)
            throw DataAccessException("Ошибка при получении списка неоплаченных счетов.", e)
        } finally {
            ConnectionPool.releaseConnection(connection)
        }
        return invoices
    }
}