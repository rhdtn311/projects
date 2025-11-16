package com.kong.pgrouting.domain.payment

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.kong.pgrouting.domain.payment.exception.PgClientException
import org.kong.pgrouting.domain.payment.exception.PgServerException
import org.kong.pgrouting.for_test.TestRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.util.UUID

@Service
class InicisAdapter(
    private val testRepository: TestRepository,
): PaymentGatewayAdapter{
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun getName(): String {
        return "inicis"
    }

    @CircuitBreaker(name = "inicis")
    override fun preparePayment(request: PreparePaymentRequest): PreparePaymentResponse {
        // request. d
        val testInfo = testRepository.findById("inicis").get()

        try {
            if (testInfo.isSystemFailure) throw HttpServerErrorException(HttpStatusCode.valueOf(500))
            if (testInfo.isTimeout) throw ResourceAccessException("Timeout")
            val pgTransactionId = "inicis_${UUID.randomUUID()}"
            val paymentUrl = "https://mock.inicis.com/pay/${request.orderId}"

            return PreparePaymentResponse(
                pgTransactionId = pgTransactionId,
                paymentUrl = paymentUrl,
            )
        } catch (e: HttpClientErrorException) {
            log.error("[Inicis] 4xx 에러 발생: ${e.message}")
            throw PgClientException("Inicis 4xx 오류: ${e.message}")

        } catch (e: HttpServerErrorException) {
            log.error("[Inicis] 5xx 에러 발생: ${e.message}")
            throw PgServerException("Inicis 5xx 오류: ${e.message}")

        } catch (e: ResourceAccessException) {
            log.error("[Inicis] Timeout 발생: ${e.message}")
            throw PgServerException("Inicis Timeout: ${e.message}", e)
        }
    }
}