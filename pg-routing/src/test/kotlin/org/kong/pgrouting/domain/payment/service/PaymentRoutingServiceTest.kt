package org.kong.pgrouting.domain.payment.service

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.kong.pgrouting.config.GatewayProperties
import org.kong.pgrouting.config.PaymentProperties
import org.kong.pgrouting.domain.payment.PaymentGatewayAdapter

class PaymentRoutingServiceTest : DescribeSpec({
    it("지원하지 않는 결제수단이면 빈 리스트를 반환한다") {
        val registry = mockk<CircuitBreakerRegistry>()
        every { registry.allCircuitBreakers } returns emptySet()

        val adapter = mockk<PaymentGatewayAdapter>()
        every { adapter.getName() } returns "toss"

        val properties = createPaymentProperties(
            "toss" to createGatewayProperties(name = "toss", supports = listOf("CARD"))
        )

        val service = PaymentRoutingService(properties, listOf(adapter), registry)

        val result = service.getGateways("TRANSFER")
        result.shouldHaveSize(0)
    }

    it("수수료가 낮은 순으로 게이트웨이가 우선된다") {
        // given
        val registry = mockk<CircuitBreakerRegistry>()
        every { registry.allCircuitBreakers } returns emptySet()

        val toss = mockk<PaymentGatewayAdapter>()
        every { toss.getName() } returns "toss"

        val nicepay = mockk<PaymentGatewayAdapter>()
        every { nicepay.getName() } returns "nicepay"

        val inicis = mockk<PaymentGatewayAdapter>()
        every { inicis.getName() } returns "inicis"

        val properties = createPaymentProperties(
            "toss" to createGatewayProperties(
                name = "toss",
                supports = listOf("CARD"),
                fees = mapOf("CARD" to 3.0)
            ),
            "nicepay" to createGatewayProperties(
                name = "nicepay",
                supports = listOf("CARD"),
                fees = mapOf("CARD" to 2.5)
            ),
            "inicis" to createGatewayProperties(
                name = "inicis",
                supports = listOf("CARD"),
                fees = mapOf("CARD" to 2.5)
            ),
        )

        val service = PaymentRoutingService(properties, listOf(toss, nicepay, inicis), registry)

        // when
        val result = service.getGateways("CARD")

        // then
        result.shouldHaveSize(3)
        val names = result.map { it.getName() }
        val firstTwo = names.take(2)
        firstTwo.size shouldBe 2
        firstTwo.shouldContainAll("nicepay", "inicis")
        names[2] shouldBe "toss"
    }


})

private fun createPaymentProperties(vararg entries: Pair<String, GatewayProperties>): PaymentProperties =
    PaymentProperties(
        timeoutSeconds = 30,
        webhookUrl = "http://localhost/webhook",
        gateways = mapOf(*entries)
    )

private fun createGatewayProperties(
    name: String,
    apiKey: String = "random key",
    supports: List<String> = listOf("CREDIT_CARD", "BANK_TRANSFER", "NAVER_PAY"),
    fees: Map<String, Double> = emptyMap()
): GatewayProperties = GatewayProperties(name, apiKey, supports, fees)