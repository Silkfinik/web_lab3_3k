package org.example.entity

import jakarta.persistence.*

@Entity
@Table(name = "subscribers")
@NamedQueries(
    value = [
        NamedQuery(name = "Subscriber.findById", query = "SELECT s FROM Subscriber s WHERE s.id = :id"),
        NamedQuery(name = "Subscriber.findAll", query = "SELECT s FROM Subscriber s"),
        NamedQuery(name = "Subscriber.block", query = "UPDATE Subscriber s SET s.isBlocked = TRUE WHERE s.id = :id"),
        NamedQuery(name = "Subscriber.deleteAll", query = "DELETE FROM Subscriber s")
    ]
)
data class Subscriber(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int = 0,

    val name: String = "",

    @Column(name = "phone_number", unique = true)
    val phoneNumber: String = "",

    val balance: Double = 0.0,

    @Column(name = "is_blocked")
    var isBlocked: Boolean = false,

    @OneToMany(mappedBy = "subscriber", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val invoices: MutableList<Invoice> = mutableListOf(),

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "subscriber_services",
        joinColumns = [JoinColumn(name = "subscriber_id")],
        inverseJoinColumns = [JoinColumn(name = "service_id")]
    )
    val services: MutableSet<Service> = mutableSetOf()
)