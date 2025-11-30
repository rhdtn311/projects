package org.kong.pgrouting.domain.payment.service

import org.springframework.stereotype.Component

@Component
class FeeBasedRoutingStrategy : GatewayRoutingStrategy {
    override val strategy: SortStrategy
        get() = SortStrategy.FEE

    override fun sort(candidates: List<RoutingCandidate>, paymentMethod: String): List<RoutingCandidate> {
        val groupedByFee: Map<Double, List<RoutingCandidate>> = candidates.groupBy { candidate ->
            candidate.config.fees[paymentMethod] ?: Double.MAX_VALUE
        }

        return groupedByFee.toSortedMap().values.flatMap { sameFeeCandidates ->
            if (sameFeeCandidates.size > 1) sameFeeCandidates.shuffled() else sameFeeCandidates
        }
    }
}
