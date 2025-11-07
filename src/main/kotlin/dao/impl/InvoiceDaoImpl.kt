package org.example.dao.impl

import org.example.dao.api.InvoiceDao
import org.example.db.JpaManager
import org.example.entity.Invoice
import org.example.entity.Invoice_
import org.example.entity.Subscriber_
import org.slf4j.LoggerFactory
import org.example.exception.*
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceException

class InvoiceDaoImpl : InvoiceDao {

    private val logger = LoggerFactory.getLogger(InvoiceDaoImpl::class.java)

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
            val cb = em.criteriaBuilder
            val cq = cb.createQuery(Invoice::class.java)
            val root = cq.from(Invoice::class.java)

            cq.where(cb.equal(root.get(Invoice_.subscriber).get(Subscriber_.id), subscriberId))

            return em.createQuery(cq).resultList

        } catch (e: Exception) {
            logger.error("Failed to find invoices for subscriber $subscriberId", e)
            throw DataAccessException("Ошибка при поиске счетов абонента.", e)
        } finally {
            em.close()
        }
    }

    override fun pay(invoiceId: Int): Boolean {
        return executeInTransaction { em ->
            val cb = em.criteriaBuilder

            val cu = cb.createCriteriaUpdate(Invoice::class.java)
            val root = cu.from(Invoice::class.java)

            cu.set(root.get(Invoice_.isPaid), true)

            cu.where(cb.equal(root.get(Invoice_.id), invoiceId))

            val rowsAffected = em.createQuery(cu).executeUpdate()

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
            val cb = em.criteriaBuilder
            val cq = cb.createQuery(Int::class.java)
            val root = cq.from(Invoice::class.java)

            cq.select(root.get(Invoice_.subscriber).get(Subscriber_.id))

            cq.where(cb.equal(root.get(Invoice_.id), invoiceId))

            return em.createQuery(cq).singleResult

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
            val cb = em.criteriaBuilder
            val cq = cb.createQuery(Invoice::class.java)
            val root = cq.from(Invoice::class.java)

            cq.where(cb.equal(root.get(Invoice_.isPaid), false))

            return em.createQuery(cq).resultList

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