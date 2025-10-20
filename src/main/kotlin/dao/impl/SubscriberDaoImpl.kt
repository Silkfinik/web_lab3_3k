package org.example.dao.impl

import org.example.dao.api.SubscriberDao
import org.example.db.JpaManager
import org.example.entity.Subscriber
import org.slf4j.LoggerFactory
import org.example.exception.*
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceException

class SubscriberDaoImpl : SubscriberDao {

    private val logger = LoggerFactory.getLogger(SubscriberDaoImpl::class.java)

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

    override fun findById(id: Int): Subscriber? {
        val em = JpaManager.getEntityManager()

        val subscriber: Subscriber?

        try {
            subscriber = em.createNamedQuery("Subscriber.findById", Subscriber::class.java)
                .setParameter("id", id)
                .singleResult
        } catch (_: jakarta.persistence.NoResultException) {
            return null
        } catch (e: Exception) {
            logger.error("Failed to find subscriber by id $id", e)
            throw DataAccessException("Ошибка при поиске абонента.", e)
        } finally {
            em.close()
        }

        return subscriber
    }

    override fun findAll(): List<Subscriber> {
        val em = JpaManager.getEntityManager()
        try {
            return em.createNamedQuery("Subscriber.findAll", Subscriber::class.java)
                .resultList
        } catch (e: Exception) {
            logger.error("Failed to find all subscribers", e)
            throw DataAccessException("Ошибка при получении списка абонентов.", e)
        } finally {
            em.close()
        }
    }

    override fun block(subscriberId: Int) {
        executeInTransaction { em ->
            val updatedCount = em.createNamedQuery("Subscriber.block")
                .setParameter("id", subscriberId)
                .executeUpdate()

            if (updatedCount == 0) {
                throw EntryNotFoundException("Абонент с ID $subscriberId не найден.")
            }
            logger.info("Subscriber $subscriberId was blocked.")
        }
    }

    override fun add(subscriber: Subscriber): Subscriber {
        return executeInTransaction { em ->
            try {
                em.persist(subscriber)
                logger.info("New subscriber created with id ${subscriber.id}")
                return@executeInTransaction subscriber
            } catch (e: PersistenceException) {
                if (e.message?.contains("UNIQUE_") == true) {
                    throw DuplicateEntryException("Абонент с номером ${subscriber.phoneNumber} уже существует.", e)
                }
                throw DataAccessException("Ошибка при добавлении абонента.", e)
            }
        }
    }

    override fun deleteAll() {
        executeInTransaction { em ->
            em.createNamedQuery("Subscriber.deleteAll").executeUpdate()
        }
    }
}