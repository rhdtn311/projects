package org.kong.pgrouting.domain.payment.service

import org.kong.pgrouting.config.GatewayProperties
import org.kong.pgrouting.domain.payment.PaymentGatewayAdapter

data class RoutingCandidate(
    val name: String,
    val config: GatewayProperties,
    val adapter: PaymentGatewayAdapter,
)
