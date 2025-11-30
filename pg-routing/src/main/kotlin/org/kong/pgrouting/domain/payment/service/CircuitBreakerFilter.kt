package org.kong.pgrouting.domain.payment.service

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CircuitBreakerFilter(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) : GatewayFilter {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun filter(candidates: List<RoutingCandidate>, paymentMethod: String): List<RoutingCandidate> {
        val openCircuits: Set<String> = circuitBreakerRegistry.allCircuitBreakers
            .filter { it.state == CircuitBreaker.State.OPEN }
            .map { it.name }
            .toSet()

        if (openCircuits.isNotEmpty()) {
            log.warn("현재 차단된 PG사: {}", openCircuits)
        }

        return candidates.filterNot { openCircuits.contains(it.name) }
    }
}
