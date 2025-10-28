package org.example.pgrouting.domain.payment.service

import org.example.pgrouting.config.GatewayProperties
import org.example.pgrouting.config.PaymentProperties
import org.example.pgrouting.domain.payment.PaymentGatewayAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentRoutingService(
    private val properties: PaymentProperties,
    adapters: List<PaymentGatewayAdapter>,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val adapterMap: Map<String, PaymentGatewayAdapter> =
        adapters.associateBy { it.getName() }

    fun getGateways(paymentMethod: String): List<PaymentGatewayAdapter> {
        val gatewaysConfig = properties.gateways

        // 1. 결제 수단 찾기
        val supportedPaymentGateways = gatewaysConfig.filter { (_, config) -> config.supports.contains(paymentMethod) }
        if (supportedPaymentGateways.isEmpty()) {
            log.warn("[$paymentMethod] 결제 수단을 지원하는 PG사를 찾을 수 없습니다.")
            return emptyList()
        }

        // 2. 수수료 파악
        val sortedGroups = supportedPaymentGateways.entries
            .groupBy { (_, config) ->
                config.fees[paymentMethod] ?: Double.MAX_VALUE
            }.toSortedMap()

        // 3. 우선순위가 같은 PG사끼리 라운드 로빈
        val prioritizedList = sortedGroups.flatMap { (fee, gatewaysInGroup) ->
            if (gatewaysInGroup.size == 1) {
                gatewaysInGroup
            } else {
                applyRoundRobin(gatewaysInGroup)
            }
        }

        return prioritizedList.mapNotNull { (pgName, _) -> adapterMap[pgName] }
    }

    private fun applyRoundRobin(
        gatewaysInGroup: List<Map.Entry<String, GatewayProperties>>,
    ): List<Map.Entry<String, GatewayProperties>> {
        // TODO: 라운드로빈 구현
        return gatewaysInGroup
    }
}