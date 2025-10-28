package org.example.pgrouting.domain.payment

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.example.pgrouting.domain.payment.exception.PgClientException
import org.example.pgrouting.domain.payment.exception.PgServerException
import org.example.pgrouting.for_test.TestRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.util.UUID

@Service
class TossAdapter(
    private val testRepository: TestRepository,
): PaymentGatewayAdapter{
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun getName(): String {
        return "toss"
    }

    @CircuitBreaker(name = "toss")
    override fun preparePayment(request: PreparePaymentRequest): PreparePaymentResponse {
        val testInfo = testRepository.findById("toss").get()

        try {
            if (testInfo.isSystemFailure) throw HttpServerErrorException(HttpStatusCode.valueOf(500))
            if (testInfo.isTimeout) throw ResourceAccessException("Timeout")

            val pgTransactionId = "toss_${UUID.randomUUID()}"
            val paymentUrl = "https://mock.tosspayments.com/pay/${request.orderId}"

            return PreparePaymentResponse(
                pgTransactionId = pgTransactionId,
                paymentUrl = paymentUrl
            )
        } catch (e: HttpClientErrorException) {
            log.error("[Toss] 4xx 에러 발생: ${e.message}")
            throw PgClientException("Toss 4xx 오류: ${e.message}")

        } catch (e: HttpServerErrorException) {
            log.error("[Toss] 5xx 에러 발생: ${e.message}")
            throw PgServerException("Toss 5xx 오류: ${e.message}")

        } catch (e: ResourceAccessException) {
            log.error("[Toss] Timeout 발생: ${e.message}")
            throw PgServerException("Toss Timeout: ${e.message}", e)
        }
    }
}