package org.example.dao.api
import org.example.entity.Invoice

interface InvoiceDao {
    fun findBySubscriberId(subscriberId: Int): List<Invoice>
    fun pay(invoiceId: Int): Boolean
    fun findSubscriberIdByInvoiceId(invoiceId: Int): Int?
    fun findUnpaid(): List<Invoice>
    fun add(invoice: Invoice): Invoice
}