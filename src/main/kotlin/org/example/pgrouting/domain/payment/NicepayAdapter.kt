package org.example.pgrouting.domain.payment

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NicepayAdapter: PaymentGatewayAdapter {
    override fun getName(): String {
        return "nicepay"
    }

    override fun preparePayment(request: PreparePaymentRequest): PreparePaymentResponse {
        val pgTransactionId = "nicepay_${UUID.randomUUID()}"
        val paymentUrl = "https://mock.nicepay.com/pay/${request.orderId}"

        return PreparePaymentResponse(
            pgTransactionId = pgTransactionId,
            paymentUrl = paymentUrl
        )
    }
}