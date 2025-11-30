package org.kong.pgrouting.domain.payment.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SupportedMethodFilter : GatewayFilter {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun filter(candidates: List<RoutingCandidate>, paymentMethod: String): List<RoutingCandidate> {
        val (supported, unsupported) = candidates.partition { candidate ->
            candidate.config.supports.contains(paymentMethod)
        }

        if (unsupported.isNotEmpty()) {
            val names = unsupported.map { it.name }
            log.debug("[$paymentMethod] 미지원 PG사 필터링됨: {}", names)
        }

        if (supported.isEmpty()) {
            log.warn("[$paymentMethod] 결제 수단을 지원하는 PG사를 찾을 수 없습니다.")
        }

        return supported
    }
}
