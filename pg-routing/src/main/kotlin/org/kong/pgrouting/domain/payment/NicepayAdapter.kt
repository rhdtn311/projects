package org.kong.pgrouting.domain.payment

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.kong.pgrouting.service.exception.PgClientException
import org.kong.pgrouting.service.exception.PgServerException
import org.kong.pgrouting.test.TestRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.util.UUID

@Service
class NicepayAdapter(
    private val testRepository: TestRepository,
): PaymentGatewayAdapter {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun getName(): String {
        return "nicepay"
    }

    @CircuitBreaker(name = "nicepay")
    override fun preparePayment(request: PreparePaymentRequest): PreparePaymentResponse {
        val testInfo = testRepository.findById("nicepay").get()

        try {
            if (testInfo.isSystemFailure) throw HttpServerErrorException(HttpStatusCode.valueOf(500))
            if (testInfo.isTimeout) throw ResourceAccessException("Timeout")

            val pgTransactionId = "nicepay_${UUID.randomUUID()}"
            val paymentUrl = "https://mock.nicepay.com/pay/${request.orderId}"

            return PreparePaymentResponse(
                pgTransactionId = pgTransactionId,
                paymentUrl = paymentUrl
            )
        } catch (e: HttpClientErrorException) {
            log.error("[Nice] 4xx 에러 발생: ${e.message}")
            throw PgClientException("Nice 4xx 오류: ${e.message}")

        } catch (e: HttpServerErrorException) {
            log.error("[Nice] 5xx 에러 발생: ${e.message}")
            throw PgServerException("Nice 5xx 오류: ${e.message}")

        } catch (e: ResourceAccessException) {
            log.error("[Nice] Timeout 발생: ${e.message}")
            throw PgServerException("Nice Timeout: ${e.message}", e)
        }
    }
}