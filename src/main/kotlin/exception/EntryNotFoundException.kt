package org.example.exception

/**
 * Выбрасывается, когда операция не может быть выполнена,
 * так как ожидаемая запись (e.g., по ID) не была найдена.
 */
class EntryNotFoundException(message: String, cause: Throwable? = null) : DataAccessException(message, cause)