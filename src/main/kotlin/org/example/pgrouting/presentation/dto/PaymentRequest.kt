package org.example.pgrouting.presentation.dto

data class PaymentRequest(
    val amount: Long,
    val productName: String,
    val paymentMethod: String,
)