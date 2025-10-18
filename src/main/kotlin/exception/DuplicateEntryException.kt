package org.example.exception

/**
 * Исключение, выбрасываемое при попытке вставить дублирующуюся запись (нарушение UNIQUE или PRIMARY KEY).
 */
class DuplicateEntryException(message: String, cause: Throwable? = null) : DataAccessException(message, cause)