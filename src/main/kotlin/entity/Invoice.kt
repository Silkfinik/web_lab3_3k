package org.example.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "invoices")
data class Invoice(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int = 0,

    val amount: Double = 0.0,

    @Column(name = "issue_date")
    val issueDate: LocalDate = LocalDate.now(),

    @Column(name = "is_paid")
    var isPaid: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id")
    val subscriber: Subscriber? = null
)