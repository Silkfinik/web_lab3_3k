package org.example.entity

data class Subscriber(
    val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val balance: Double,
    val isBlocked: Boolean
)