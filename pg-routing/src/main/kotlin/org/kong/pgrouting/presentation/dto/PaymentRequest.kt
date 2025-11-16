package org.kong.pgrouting.presentation.dto

import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Length

data class PaymentRequest(
    @field:Size(min = 1, max = 100)
    val amount: Long,
    @field:Length(min = 1, max = 255)
    val productName: String,
    @field:Length(min = 1, max = 50)
    val paymentMethod: String,
)