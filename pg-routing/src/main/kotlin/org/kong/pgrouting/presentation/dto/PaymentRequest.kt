package org.kong.pgrouting.presentation.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.Length

data class PaymentRequest(
    @field:Min(1)
    @field:Max(1000)
    val amount: Long,
    @field:Length(min = 1, max = 255)
    val productName: String,
    @field:Length(min = 1, max = 50)
    val paymentMethod: String,
)