package org.example.dao.impl

import org.example.dao.api.ServiceDao
import org.example.db.JpaManager
import org.example.entity.Service
import org.example.entity.Subscriber
import org.slf4j.LoggerFactory
import java.sql.SQLException
import org.example.exception.*
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceException

class ServiceDaoImpl : ServiceDao {

    private val logger = LoggerFactory.getLogger(ServiceDaoImpl::class.java)

    // Коды ошибок H2 из вашего старого DAO
    private companion object {
        const val H2_DUPLICATE_KEY_CODE = 23505
        const val H2_INTEGRITY_VIOLATION_CODE = 23503
    }

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
            em.transaction.rollback()
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

    override fun findAll(): List<Service> {
        val em = JpaManager.getEntityManager()
        try {
            // Используем NamedQuery "Service.findAll" из Service.kt
            return em.createNamedQuery("Service.findAll", Service::class.java)
                .resultList
        } catch (e: Exception) {
            logger.error("Failed to find all services", e)
            throw DataAccessException("Ошибка при получении списка услуг", e)
        } finally {
            em.close()
        }
    }

    override fun findBySubscriberId(subscriberId: Int): List<Service> {
        val em = JpaManager.getEntityManager()
        try {
            // Используем NamedQuery "Service.findBySubscriberId" из Service.kt
            return em.createNamedQuery("Service.findBySubscriberId", Service::class.java)
                .setParameter("subscriberId", subscriberId)
                .resultList
        } catch (e: Exception) {
            logger.error("Failed to find services for subscriber $subscriberId", e)
            throw DataAccessException("Ошибка при поиске услуг абонента.", e)
        } finally {
            em.close()
        }
    }

    override fun linkServiceToSubscriber(subscriberId: Int, serviceId: Int) {
        executeInTransaction { em ->
            try {
                // Находим обе сущности, которыми хотим управлять
                val subscriber = em.find(Subscriber::class.java, subscriberId)
                val service = em.find(Service::class.java, serviceId)

                if (subscriber == null) {
                    throw EntryNotFoundException("Абонент с ID $subscriberId не найден.")
                }
                if (service == null) {
                    throw EntryNotFoundException("Услуга с ID $serviceId не найдена.")
                }

                // Так как связь @ManyToMany управляется со стороны Subscriber,
                // мы просто добавляем услугу в его коллекцию
                if (subscriber.services.contains(service)) {
                    throw DuplicateEntryException("Эта услуга уже подключена абоненту.")
                }

                subscriber.services.add(service)

                // Сохраняем изменения
                em.merge(subscriber)

                logger.info("Service $serviceId linked to subscriber $subscriberId")

            } catch (e: PersistenceException) {
                logger.error("Failed to link service $serviceId to subscriber $subscriberId", e)
                // Обрабатываем ошибки по аналогии со старым кодом
                // (хотя JPA делает часть проверок за нас)
                val cause = e.cause
                if (cause is SQLException) {
                    when (cause.errorCode) {
                        H2_DUPLICATE_KEY_CODE ->
                            throw DuplicateEntryException("Эта услуга уже подключена абоненту.", e)
                        H2_INTEGRITY_VIOLATION_CODE ->
                            throw EntryNotFoundException("Абонент (ID $subscriberId) или услуга (ID $serviceId) не найдены.", e)
                    }
                }
                throw DataAccessException("Ошибка при подключении услуги.", e)
            }
        }
    }
}