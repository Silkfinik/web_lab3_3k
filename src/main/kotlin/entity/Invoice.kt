package org.example.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "invoices")
@NamedQueries(
    value = [
        NamedQuery(name = "Invoice.findBySubscriberId", query = "SELECT i FROM Invoice i WHERE i.subscriber.id = :subscriberId"),
        NamedQuery(name = "Invoice.pay", query = "UPDATE Invoice i SET i.isPaid = TRUE WHERE i.id = :id"),
        NamedQuery(name = "Invoice.findSubscriberIdById", query = "SELECT i.subscriber.id FROM Invoice i WHERE i.id = :id"),
        NamedQuery(name = "Invoice.findUnpaid", query = "SELECT i FROM Invoice i WHERE i.isPaid = FALSE")
    ]
)
data class Invoice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    val amount: Double = 0.0, // <-- ДОБАВЛЕН DEFAULT

    @Column(name = "issue_date")
    val issueDate: LocalDate = LocalDate.now(), // <-- ДОБАВЛЕН DEFAULT

    @Column(name = "is_paid")
    var isPaid: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id")
    // ❗️ ВАЖНО: Сделано nullable и добавлен default
    val subscriber: Subscriber? = null
)