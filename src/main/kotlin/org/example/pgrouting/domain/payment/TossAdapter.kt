package org.example.pgrouting.domain.payment

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TossAdapter: PaymentGatewayAdapter{

    override fun getName(): String {
        return "toss"
    }

    override fun preparePayment(request: PreparePaymentRequest): PreparePaymentResponse {
        val pgTransactionId = "toss_${UUID.randomUUID()}"
        val paymentUrl = "https://mock.tosspayments.com/pay/${request.orderId}"

        return PreparePaymentResponse(
            pgTransactionId = pgTransactionId,
            paymentUrl = paymentUrl
        )
    }
}