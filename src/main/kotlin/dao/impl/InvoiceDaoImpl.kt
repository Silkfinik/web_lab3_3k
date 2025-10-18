package org.example.dao.impl

import org.example.dao.api.InvoiceDao
import org.example.db.JdbcConnector
import org.example.entity.Invoice

class InvoiceDaoImpl : InvoiceDao {

    private companion object {
        const val FIND_BY_SUBSCRIBER_ID = "SELECT * FROM invoices WHERE subscriber_id = ?"
        const val PAY_INVOICE = "UPDATE invoices SET is_paid = TRUE WHERE id = ?"
        const val FIND_SUBSCRIBER_ID_BY_INVOICE_ID = "SELECT subscriber_id FROM invoices WHERE id = ?"
        const val FIND_UNPAID = "SELECT * FROM invoices WHERE is_paid = FALSE"
    }

    override fun findBySubscriberId(subscriberId: Int): List<Invoice> {
        val invoices = mutableListOf<Invoice>()
        JdbcConnector.getConnection().use { connection ->
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
        }
        return invoices
    }

    override fun pay(invoiceId: Int): Boolean {
        JdbcConnector.getConnection().use { connection ->
            connection.prepareStatement(PAY_INVOICE).use { statement ->
                statement.setInt(1, invoiceId)
                val rowsAffected = statement.executeUpdate()
                return rowsAffected > 0
            }
        }
    }

    override fun findSubscriberIdByInvoiceId(invoiceId: Int): Int? {
        JdbcConnector.getConnection().use { connection ->
            connection.prepareStatement(FIND_SUBSCRIBER_ID_BY_INVOICE_ID).use { statement ->
                statement.setInt(1, invoiceId)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    return rs.getInt("subscriber_id")
                }
            }
        }
        return null
    }

    override fun findUnpaid(): List<Invoice> {
        val invoices = mutableListOf<Invoice>()
        JdbcConnector.getConnection().use { connection ->
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
        }
        return invoices
    }
}