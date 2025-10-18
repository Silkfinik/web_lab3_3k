package org.example.main

import org.example.dao.api.InvoiceDao
import org.example.dao.api.ServiceDao
import org.example.dao.api.SubscriberDao
import org.example.dao.impl.InvoiceDaoImpl
import org.example.dao.impl.ServiceDaoImpl
import org.example.dao.impl.SubscriberDaoImpl
import org.example.db.DataInitializer
import org.example.db.DatabaseManager
import org.example.entity.Subscriber
import java.io.File
import java.util.Scanner

val subscriberDao: SubscriberDao = SubscriberDaoImpl()
val serviceDao: ServiceDao = ServiceDaoImpl()
val invoiceDao: InvoiceDao = InvoiceDaoImpl()
val scanner = Scanner(System.`in`)

fun main() {
    println("Добро пожаловать в Telecom App!")
    println("Выберите режим работы:")
    println("  1. Начать с чистой базой данных (все изменения будут удалены)")
    println("  2. Продолжить работу с существующими данными")
    print("Ваш выбор [1 или 2]: ")

    when (readlnOrNull()) {
        "1" -> {
            println("\nИнициализация чистой базы данных...")
            try {
                val dbFile = File("telecom.mv.db")
                if (dbFile.exists()) {
                    dbFile.delete()
                }
                DatabaseManager.initDatabase()
                DataInitializer.insertInitialData()
                println("Новая база данных успешно создана и заполнена тестовыми данными.")
            } catch (e: Exception) {
                println("Ошибка при инициализации базы данных: ${e.message}")
                e.printStackTrace()
                return
            }
        }
        "2" -> {
            println("\nПодключение к существующей базе данных...")
        }
        else -> {
            println("Неверный выбор. Выход из приложения.")
            return
        }
    }

    println("\n" + "-".repeat(40))

    while (true) {
        printMenu()
        print("Выберите опцию: ")
        val choice = scanner.nextInt()

        when (choice) {
            1 -> askAndShowSubscriberServices()
            2 -> askAndShowSubscriberInvoices()
            3 -> showAllServices()
            4 -> payInvoice()
            5 -> blockSubscriber()
            6 -> showAllSubscribers()
            7 -> showSubscriberDetails()
            8 -> addSubscriber()
            9 -> showUnpaidInvoices()
            10 -> connectServiceToSubscriber()
            0 -> {
                println("Выход из приложения.")
                return
            }
            else -> println("Неверная опция, попробуйте снова.")
        }
        println("-".repeat(40))
    }
}

fun printMenu() {
    println("1. Показать услуги абонента")
    println("2. Показать счета абонента")
    println("3. Показать все доступные услуги")
    println("4. Оплатить счет")
    println("5. Заблокировать абонента")
    println("6. Показать всех абонентов")
    println("7. Детальная информация об абоненте")
    println("8. Добавить нового абонента")
    println("9. Показать неоплаченные счета")
    println("10. Подключить услугу абоненту")
    println("0. Выход")
}

private fun askAndShowSubscriberServices() {
    print("Введите ID абонента: ")
    val id = readlnOrNull()?.toIntOrNull()
    if (id != null) {
        showSubscriberServices(id)
    } else {
        println("Некорректный ввод ID.")
    }
}

private fun showSubscriberServices(subscriberId: Int) {
    val services = serviceDao.findBySubscriberId(subscriberId)
    if (services.isEmpty()) {
        println("У абонента с ID $subscriberId нет подключенных услуг.")
    } else {
        println("Текущие услуги абонента ID $subscriberId:")
        services.forEach { println("  - ${it.name} (${it.monthlyFee} руб./мес.)") }
    }
}

private fun showSubscriberInvoices(subscriberId: Int) {
    val invoices = invoiceDao.findBySubscriberId(subscriberId)
    if (invoices.isEmpty()) {
        println("Счета для абонента с ID $subscriberId не найдены.")
    } else {
        println("Обновленный список счетов абонента ID $subscriberId:")
        invoices.forEach {
            val status = if (it.isPaid) "Оплачен" else "НЕ ОПЛАЧЕН"
            println("  - Счет №${it.id} от ${it.issueDate} на сумму ${it.amount} руб. Статус: $status")
        }
    }
}

private fun askAndShowSubscriberInvoices() {
    print("Введите ID абонента: ")
    val id = readlnOrNull()?.toIntOrNull()
    if (id == null) {
        println("Некорректный ввод ID.")
        return
    }
    showSubscriberInvoices(id)
}

private fun showAllServices() {
    val services = serviceDao.findAll()
    println("Список всех доступных услуг:")
    services.forEach {
        println("  - [ID: ${it.id}] ${it.name} (${"%.2f".format(it.monthlyFee)} руб./мес.)")
    }
}
private fun payInvoice() {
    print("Введите ID счета для оплаты: ")
    val id = readlnOrNull()?.toIntOrNull()
    if (id == null) {
        println("Некорректный ввод ID.")
        return
    }

    val isPaid = invoiceDao.pay(id)

    if (isPaid) {
        println("Счет №$id был успешно оплачен.")
        val subscriberId = invoiceDao.findSubscriberIdByInvoiceId(id)
        if (subscriberId != null) {
            showSubscriberInvoices(subscriberId)
        }
    } else {
        println("❌ Ошибка: Счет с ID $id не найден.")
    }
}

private fun blockSubscriber() {
    print("Введите ID абонента для блокировки: ")
    val id = scanner.nextInt()
    subscriberDao.block(id)
    println("Абонент с ID $id был заблокирован.")
}

private fun showAllSubscribers() {
    val subscribers = subscriberDao.findAll()
    if (subscribers.isEmpty()) {
        println("В системе нет зарегистрированных абонентов.")
    } else {
        println("Список всех абонентов:")
        println("%-5s %-20s %-15s %-10s %-10s".format("ID", "Имя", "Телефон", "Баланс", "Статус"))
        subscribers.forEach {
            val status = if (it.isBlocked) "Заблокирован" else "Активен"
            println("%-5d %-20s %-15s %-10.2f %-10s".format(it.id, it.name, it.phoneNumber, it.balance, status))
        }
    }
}

private fun showSubscriberDetails() {
    print("Введите ID абонента: ")
    val id = readlnOrNull()?.toIntOrNull()
    if (id == null) {
        println("Некорректный ввод ID.")
        return
    }

    val subscriber = subscriberDao.findById(id)
    if (subscriber == null) {
        println("Абонент с ID $id не найден.")
    } else {
        println("Детальная информация по абоненту ID ${subscriber.id}:")
        println("  Имя: ${subscriber.name}")
        println("  Телефон: ${subscriber.phoneNumber}")
        println("  Баланс: ${"%.2f".format(subscriber.balance)} руб.")
        println("  Статус: ${if (subscriber.isBlocked) "Заблокирован" else "Активен"}")
    }
}

private fun addSubscriber() {
    print("Введите имя нового абонента: ")
    val name = readln()

    print("Введите номер телефона (+375...): ")
    val phone = readln()

    val newSubscriber = Subscriber(name = name, phoneNumber = phone, balance = 0.0, isBlocked = false)
    val created = subscriberDao.add(newSubscriber)

    println("Абонент успешно создан с ID: ${created.id}")
}

private fun showUnpaidInvoices() {
    val invoices = invoiceDao.findUnpaid()
    if (invoices.isEmpty()) {
        println("Все счета оплачены.")
    } else {
        println("Список неоплаченных счетов:")
        invoices.forEach {
            println("  - Счет №${it.id} (абонент ID ${it.subscriberId}) на сумму ${it.amount} руб.")
        }
    }
}

private fun connectServiceToSubscriber() {
    println("Выберите абонента для подключения услуги:")
    showAllSubscribers()
    print("Введите ID абонента: ")
    val subscriberId = readlnOrNull()?.toIntOrNull()
    if (subscriberId == null) {
        println("Некорректный ввод.")
        return
    }

    println("\nВыберите услугу для подключения:")
    showAllServices()
    print("Введите ID услуги: ")
    val serviceId = readlnOrNull()?.toIntOrNull()
    if (serviceId == null) {
        println("Некорректный ввод.")
        return
    }

    try {
        serviceDao.linkServiceToSubscriber(subscriberId, serviceId)
        println("Услуга успешно подключена абоненту.")
        showSubscriberServices(subscriberId)
    } catch (e: Exception) {
        println("Ошибка: Не удалось подключить услугу. Возможно, она уже подключена.")
    }
}
