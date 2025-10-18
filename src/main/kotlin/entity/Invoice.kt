package org.example.entity

import java.time.LocalDate

data class Invoice(
    val id: Int = 0,
    val subscriberId: Int,
    val amount: Double,
    val issueDate: LocalDate,
    val isPaid: Boolean
)