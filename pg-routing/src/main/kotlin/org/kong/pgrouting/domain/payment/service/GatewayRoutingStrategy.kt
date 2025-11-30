package org.kong.pgrouting.domain.payment.service

interface GatewayRoutingStrategy {
    val strategy: SortStrategy
    fun sort(candidates: List<RoutingCandidate>, paymentMethod: String): List<RoutingCandidate>
}

enum class SortStrategy {
    FEE, SPECIFY
}
