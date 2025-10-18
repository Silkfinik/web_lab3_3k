package org.example.exception

/**
 * Выбрасывается, когда операция нарушает целостность данных
 * (e.g., нарушение FOREIGN KEY, NOT NULL).
 */
class DataIntegrityViolationException(message: String, cause: Throwable? = null) : DataAccessException(message, cause)