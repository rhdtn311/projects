package org.kong.pgrouting.domain.payment.service

fun interface GatewayFilter {
    fun filter(candidates: List<RoutingCandidate>, paymentMethod: String): List<RoutingCandidate>
}
