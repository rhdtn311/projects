package org.example.pgrouting.presentation

import org.example.pgrouting.presentation.dto.PaymentRequest
import org.example.pgrouting.presentation.dto.PaymentResponse
import org.example.pgrouting.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController(
    val paymentService: PaymentService,
) {

    @PostMapping("/request")
    fun requestPayment(
        @RequestBody request: PaymentRequest,
    ): ResponseEntity<PaymentResponse> {

        val response = paymentService.requestPayment(request)

        if (response.status == "FAILED") {
            return ResponseEntity.badRequest().body(response)
        }

        return ResponseEntity.ok(response)
    }
}