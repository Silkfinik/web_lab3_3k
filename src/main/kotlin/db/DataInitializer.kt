package org.example.db

import org.example.entity.Invoice
import org.example.entity.Service
import org.example.entity.Subscriber
import org.example.main.invoiceDao
import org.example.main.serviceDao
import org.example.main.subscriberDao
import org.slf4j.LoggerFactory
import java.time.LocalDate

object DataInitializer {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    fun insertInitialData() {
        println("Inserting initial data using JPA DAO...")

        try {
            subscriberDao.deleteAll()
            serviceDao.deleteAll()


            val sub1 = subscriberDao.add(
                Subscriber(name = "Иван Иванов", phoneNumber = "+375291234567", balance = 150.50)
            )
            val sub2 = subscriberDao.add(
                Subscriber(name = "Петр Петров", phoneNumber = "+375337654321", balance = -50.00, isBlocked = true)
            )

            val serv1 = serviceDao.add(Service(name = "Интернет 50 Мбит/с", monthlyFee = 450.00))
            val serv2 = serviceDao.add(Service(name = "Мобильная связь", monthlyFee = 300.00))
            val serv3 = serviceDao.add(Service(name = "Антивирус", monthlyFee = 100.00))

            serviceDao.linkServiceToSubscriber(sub1.id, serv1.id)
            serviceDao.linkServiceToSubscriber(sub1.id, serv2.id)
            serviceDao.linkServiceToSubscriber(sub2.id, serv2.id)
            serviceDao.linkServiceToSubscriber(sub2.id, serv3.id)

            invoiceDao.add(
                Invoice(
                    subscriber = sub1,
                    amount = 750.00,
                    issueDate = LocalDate.parse("2025-09-01"),
                    isPaid = true
                )
            )
            invoiceDao.add(
                Invoice(
                    subscriber = sub2,
                    amount = 400.00,
                    issueDate = LocalDate.parse("2025-09-05"),
                    isPaid = false
                )
            )

            logger.info("Initial data inserted successfully.")
            println("Initial data inserted.")

        } catch (e: Exception) {
            logger.error("Failed to insert initial data", e)
            println("Error during data initialization: ${e.message}")
            throw e
        }
    }
}