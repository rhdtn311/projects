package org.example.pgrouting.domain.payment

interface PaymentGatewayAdapter {
    fun getName(): String
    fun preparePayment(request: PreparePaymentRequest): PreparePaymentResponse
}

data class PreparePaymentRequest(
    val orderId: String,
    val amount: Long,
    val productName: String,
    val paymentMethod: String,
    val webhookUrl: String
)

data class PreparePaymentResponse(
    val pgTransactionId: String,
    val paymentUrl: String,
)