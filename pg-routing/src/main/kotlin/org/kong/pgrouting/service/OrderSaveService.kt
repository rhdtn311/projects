package org.kong.pgrouting.service

import org.kong.pgrouting.domain.entity.OrderStatus
import org.kong.pgrouting.domain.entity.PaymentOrder
import org.kong.pgrouting.domain.repository.PaymentOrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class OrderSaveService(
    private val orderRepository: PaymentOrderRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveNewOrder(request: PaymentSuccessEvent): PaymentOrder {
        return orderRepository.save(
            PaymentOrder(
                amount = request.amount,
                productName = request.productName,
                paymentMethod = request.paymentMethod,
                status = OrderStatus.PENDING,
            )
        )
    }
}

data class PaymentSuccessEvent(
    val amount: Long,
    val productName: String,
    val paymentMethod: String,
)
