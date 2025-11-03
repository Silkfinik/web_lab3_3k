package org.example.db

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.slf4j.LoggerFactory

object JpaManager {
    private val logger = LoggerFactory.getLogger(JpaManager::class.java)
    val emf: EntityManagerFactory // Фабрика Entity Manager

    init {
        logger.info("Initializing JPA EntityManagerFactory...")
        try {
            // "telecom-pu" - это имя <persistence-unit> из persistence.xml
            emf = Persistence.createEntityManagerFactory("telecom-pu")
            logger.info("EntityManagerFactory initialized successfully.")
        } catch (e: Exception) {
            logger.error("FATAL: Failed to initialize EntityManagerFactory!", e)
            throw e
        }
    }

    /**
     * Создает новый EntityManager для выполнения операций
     */
    fun getEntityManager(): EntityManager {
        return emf.createEntityManager()
    }
}