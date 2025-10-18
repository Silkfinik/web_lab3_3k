package org.example.dao.api

import org.example.entity.Subscriber

interface SubscriberDao {
    fun findById(id: Int): Subscriber?
    fun findAll(): List<Subscriber>
    fun block(subscriberId: Int)
    fun add(subscriber: Subscriber): Subscriber
}