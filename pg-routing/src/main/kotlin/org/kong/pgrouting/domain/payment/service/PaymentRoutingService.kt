package org.kong.pgrouting.domain.payment.service

import org.kong.pgrouting.config.PaymentProperties
import org.kong.pgrouting.domain.payment.PaymentGatewayAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentRoutingService(
    private val properties: PaymentProperties,
    adapters: List<PaymentGatewayAdapter>,
    private val filters: List<GatewayFilter>,
    private val sortStrategies: List<GatewayRoutingStrategy>,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val gatewaySortStrategiesMap = sortStrategies.associateBy { it.strategy }

    private val initialCandidates: List<RoutingCandidate> = adapters.mapNotNull { adapter ->
        val name = adapter.getName()
        val config = properties.gateways[name]
        if (config != null) RoutingCandidate(name, config, adapter) else null
    }

    fun getGateways(
        paymentMethod: String,
        sortStrategy: SortStrategy,
    ): List<PaymentGatewayAdapter> {
        if (initialCandidates.isEmpty()) {
            return emptyList()
        }

        var current = initialCandidates
        filters.forEach { filter ->
            current = filter.filter(current, paymentMethod)
        }

        if (current.isEmpty()) {
            log.warn("[$paymentMethod] 사용 가능한 PG사가 없습니다.")
            return emptyList()
        }

        val gatewaySortStrategy = gatewaySortStrategiesMap[sortStrategy]!!

        val sorted = gatewaySortStrategy.sort(current, paymentMethod)

        return sorted.map { it.adapter }
    }
}