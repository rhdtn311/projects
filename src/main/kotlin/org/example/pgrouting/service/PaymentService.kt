package org.example.pgrouting.service

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import jakarta.transaction.Transactional
import org.example.pgrouting.config.PaymentProperties
import org.example.pgrouting.domain.entity.OrderStatus
import org.example.pgrouting.domain.entity.PaymentHistory
import org.example.pgrouting.domain.entity.PaymentOrder
import org.example.pgrouting.domain.entity.TransactionStatus
import org.example.pgrouting.domain.payment.PaymentGatewayAdapter
import org.example.pgrouting.domain.payment.PreparePaymentRequest
import org.example.pgrouting.domain.payment.PreparePaymentResponse
import org.example.pgrouting.domain.payment.exception.PgClientException
import org.example.pgrouting.domain.payment.exception.PgServerException
import org.example.pgrouting.domain.payment.service.PaymentRoutingService
import org.example.pgrouting.domain.repository.PaymentOrderRepository
import org.example.pgrouting.domain.repository.PaymentHistoryRepository
import org.example.pgrouting.presentation.dto.PaymentRequest
import org.example.pgrouting.presentation.dto.PaymentResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val orderRepository: PaymentOrderRepository,
    private val paymentHistoryRepository: PaymentHistoryRepository,
    private val routingService: PaymentRoutingService,
    private val properties: PaymentProperties
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun requestPayment(request: PaymentRequest): PaymentResponse {
        val order = orderRepository.save(
            PaymentOrder(
                amount = request.amount,
                productName = request.productName,
                paymentMethod = request.paymentMethod,
                status = OrderStatus.PENDING,
            )
        )

        var selectedPgName: String? = null
        val adapters = routingService.getGateways(
            paymentMethod = request.paymentMethod,
        )
        if (adapters.isEmpty()) {
            order.status = OrderStatus.FAILED
            log.error("결제수단 ${request.paymentMethod}를 지원하는 PG사가 존재하지 않습니다.")
            return PaymentResponse(
                orderId = order.orderId,
                status = order.status.name,
                paymentUrl = null,
                errorMessage = "모든 PG사로부터 결제를 진행할 수 없습니다."
            )
        }

        val commonRequest = PreparePaymentRequest(
            orderId = order.orderId,
            amount = order.amount,
            productName = order.productName,
            paymentMethod = order.paymentMethod,
            webhookUrl = properties.webhookUrl
        )

        var response: PreparePaymentResponse? = null
        for (adapter in adapters) {
            try {
                response = adapter.preparePayment(commonRequest)
                selectedPgName = adapter.getName()
                savePaymentHistorySuccess(adapter, response, order)
                break
            } catch (e: PgClientException) {
                log.error("${adapter.getName()}사 결제 요청 실패", e)
                order.status = OrderStatus.FAILED
                savePaymentHistoryFail(
                    order = order,
                    adapter = adapter,
                    status = TransactionStatus.FAIL_CLIENT_ERROR,
                    exception = e
                )
                throw RuntimeException("결제 요청 정보가 올바르지 않습니다.", e)
            } catch (e: PgServerException) {
                log.warn("${adapter.getName()}사 결제 처리 중 오류 및 Timeout 발생", e)
                savePaymentHistoryFail(
                    order = order,
                    adapter = adapter,
                    status = TransactionStatus.FAIL_SERVER_ERROR,
                    exception = e
                )
                continue
            } catch (e: CallNotPermittedException) {
                log.warn("${adapter.getName()}사 오류 빈도 높으므로 사용하지 않음.", e)
                savePaymentHistoryFail(order, adapter, TransactionStatus.FAIL_SERVER_ERROR, e)
                continue
            } catch (e: Exception) {
                log.error("${adapter.getName()}사 결제 처리 중 알 수 없는 오류 발생", e)
                savePaymentHistoryFail(
                    order = order,
                    adapter = adapter,
                    status = TransactionStatus.FAIL_SERVER_ERROR,
                    exception = e
                )
                continue
            }
        }

        if (response == null) {
            order.status = OrderStatus.FAILED
            log.error("모든 PG사 결제 준비 실패. OrderId: ${order.orderId}")
            throw RuntimeException("모든 결제사 연동에 실패했습니다. 잠시 후 다시 시도해주세요.")
        }

        log.info("선택된 PG사:[${selectedPgName}]")

        return PaymentResponse(
            orderId = order.orderId,
            status = order.status.name,
            paymentUrl = response.paymentUrl,
            pgName = selectedPgName,
        )
    }

    private fun savePaymentHistoryFail(
        order: PaymentOrder,
        adapter: PaymentGatewayAdapter,
        status: TransactionStatus,
        exception: Exception,
    ) {
        val paymentHistory = PaymentHistory(
            pgName = adapter.getName(),
            status = status,
            failureReason = exception.message,
        )
        paymentHistory.paymentOrder = order
        paymentHistoryRepository.save(paymentHistory)
    }

    private fun savePaymentHistorySuccess(
        adapter: PaymentGatewayAdapter,
        response: PreparePaymentResponse,
        order: PaymentOrder
    ) {
        val paymentHistory =PaymentHistory(
            pgName = adapter.getName(),
            status = TransactionStatus.SUCCESS,
            pgTransactionId = response.pgTransactionId
        )
        paymentHistory.paymentOrder = order
        paymentHistoryRepository.save(paymentHistory)
    }
}