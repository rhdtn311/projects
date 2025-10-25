package org.example.pgrouting.domain.payment

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class InicisAdapter: PaymentGatewayAdapter{
    override fun getName(): String {
        return "inicis"
    }

    override fun preparePayment(request: PreparePaymentRequest): PreparePaymentResponse {
        val pgTransactionId = "inicis_${UUID.randomUUID()}"
        val paymentUrl = "https://mock.inicis.com/pay/${request.orderId}"

        return PreparePaymentResponse(
            pgTransactionId = pgTransactionId,
            paymentUrl = paymentUrl
        )
    }
}