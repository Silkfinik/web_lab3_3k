package org.example.dao.impl

import org.example.dao.api.InvoiceDao
import org.example.db.JpaManager
import org.example.entity.Invoice
import org.slf4j.LoggerFactory
import org.example.exception.*
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceException

class InvoiceDaoImpl : InvoiceDao {

    private val logger = LoggerFactory.getLogger(InvoiceDaoImpl::class.java)

    /**
     * Вспомогательная функция для выполнения кода внутри транзакции JPA
     */
    private fun <T> executeInTransaction(block: (em: EntityManager) -> T): T {
        val em = JpaManager.getEntityManager()
        try {
            em.transaction.begin()
            val result = block(em)
            em.transaction.commit()
            return result
        } catch (e: Exception) {
            if (em.transaction.isActive) {
                em.transaction.rollback()
            }
            logger.error("JPA transaction failed", e)
            val exceptionToThrow = when (e) {
                is PersistenceException -> DataAccessException("Ошибка доступа к данным JPA.", e)
                else -> e
            }
            throw exceptionToThrow
        } finally {
            em.close()
        }
    }

    override fun findBySubscriberId(subscriberId: Int): List<Invoice> {
        val em = JpaManager.getEntityManager()
        try {
            return em.createNamedQuery("Invoice.findBySubscriberId", Invoice::class.java)
                .setParameter("subscriberId", subscriberId)
                .resultList
        } catch (e: Exception) {
            logger.error("Failed to find invoices for subscriber $subscriberId", e)
            throw DataAccessException("Ошибка при поиске счетов абонента.", e)
        } finally {
            em.close()
        }
    }

    override fun pay(invoiceId: Int): Boolean {
        return executeInTransaction { em ->
            val rowsAffected = em.createNamedQuery("Invoice.pay")
                .setParameter("id", invoiceId)
                .executeUpdate()

            if (rowsAffected == 0) {
                logger.warn("Payment failed. Invoice with id $invoiceId not found.")
                throw EntryNotFoundException("Счет с ID $invoiceId не найден.")
            }

            logger.info("Invoice $invoiceId was paid successfully.")
            return@executeInTransaction true
        }
    }

    override fun findSubscriberIdByInvoiceId(invoiceId: Int): Int? {
        val em = JpaManager.getEntityManager()
        try {
            return em.createNamedQuery("Invoice.findSubscriberIdById", Int::class.java)
                .setParameter("id", invoiceId)
                .singleResult
        } catch (e: jakarta.persistence.NoResultException) {
            logger.warn("Subscriber ID not found for invoice $invoiceId.")
            throw EntryNotFoundException("Счет с ID $invoiceId не найден.", e)
        } catch (e: Exception) {
            logger.error("Failed to find subscriber by invoice $invoiceId", e)
            throw DataAccessException("Ошибка при поиске счета.", e)
        } finally {
            em.close()
        }
    }

    override fun findUnpaid(): List<Invoice> {
        val em = JpaManager.getEntityManager()
        try {
            return em.createNamedQuery("Invoice.findUnpaid", Invoice::class.java)
                .resultList
        } catch (e: Exception) {
            logger.error("Failed to find unpaid invoices", e)
            throw DataAccessException("Ошибка при получении списка неоплаченных счетов.", e)
        } finally {
            em.close()
        }
    }

    override fun add(invoice: Invoice): Invoice {
        return executeInTransaction { em ->
            try {
                em.persist(invoice)
                return@executeInTransaction invoice
            } catch (e: PersistenceException) {
                throw DataAccessException("Ошибка при добавлении счета.", e)
            }
        }
    }
}