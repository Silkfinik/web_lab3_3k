package org.example.dao.api
import org.example.entity.Service

interface ServiceDao {
    fun findAll(): List<Service>
    fun findBySubscriberId(subscriberId: Int): List<Service>
    fun linkServiceToSubscriber(subscriberId: Int, serviceId: Int)
    fun add(service: Service): Service
    fun deleteAll()
}