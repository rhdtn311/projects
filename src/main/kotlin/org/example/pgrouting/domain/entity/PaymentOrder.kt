package org.example.pgrouting.domain.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "payment_orders")
class PaymentOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val orderId: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    val productName: String,

    @Column(nullable = false)
    val paymentMethod: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @OneToMany(mappedBy = "paymentOrder")
    val transactions: MutableList<PaymentHistory> = mutableListOf()

) : BaseEntity() {
    fun addTransaction(transaction: PaymentHistory) {
        this.transactions.add(transaction)
        transaction.paymentOrder = this
    }
}

enum class OrderStatus {
    PENDING,
    PAID,
    FAILED,
}