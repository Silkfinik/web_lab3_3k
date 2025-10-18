package org.example.exception

/**
 * Общее исключение для всех ошибок, связанных с доступом к данным.
 * @param message Понятное сообщение для пользователя
 * @param cause Оригинальное исключение (для логгирования)
 */
open class DataAccessException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)