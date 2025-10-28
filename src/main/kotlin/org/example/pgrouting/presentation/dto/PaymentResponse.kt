package org.example.pgrouting.presentation.dto

data class PaymentResponse(
    val orderId: String,
    val status: String,
    val paymentUrl: String?,
    val errorMessage: String? = null,
    val pgName: String? = null,
)
