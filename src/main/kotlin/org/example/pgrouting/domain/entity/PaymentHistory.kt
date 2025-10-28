package org.example.pgrouting.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "payment_transactions")
class PaymentHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val pgName: String,

    @Column(nullable = false)
    val pgTransactionId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TransactionStatus,

    @Column(nullable = true)
    val failureReason: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id", nullable = false)
    var paymentOrder: PaymentOrder? = null

) : BaseEntity()

enum class TransactionStatus {
    SUCCESS,
    FAIL_SERVER_ERROR,
    FAIL_CLIENT_ERROR,
}