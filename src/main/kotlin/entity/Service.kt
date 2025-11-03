package org.example.entity

import jakarta.persistence.*

@Entity
@Table(name = "services")
@NamedQueries(
    value = [
        NamedQuery(name = "Service.findAll", query = "SELECT s FROM Service s"),
        NamedQuery(name = "Service.findBySubscriberId", query = "SELECT s FROM Service s JOIN s.subscribers sub WHERE sub.id = :subscriberId"),
        NamedQuery(name = "Service.deleteAll", query = "DELETE FROM Service s")
    ]
)
data class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(unique = true)
    val name: String = "", // <-- ДОБАВЛЕН DEFAULT

    @Column(name = "monthly_fee")
    val monthlyFee: Double = 0.0, // <-- ДОБАВЛЕН DEFAULT

    @ManyToMany(mappedBy = "services", fetch = FetchType.LAZY)
    val subscribers: MutableSet<Subscriber> = mutableSetOf()
)