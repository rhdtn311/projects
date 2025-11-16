package org.kong.pgrouting.service

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import jakarta.transaction.Transactional
import org.kong.pgrouting.config.PaymentProperties
import org.kong.pgrouting.domain.entity.OrderStatus
import org.kong.pgrouting.domain.entity.PaymentHistory
import org.kong.pgrouting.domain.entity.PaymentOrder
import org.kong.pgrouting.domain.entity.TransactionStatus
import org.kong.pgrouting.domain.payment.PaymentGatewayAdapter
import org.kong.pgrouting.domain.payment.PreparePaymentRequest
import org.kong.pgrouting.domain.payment.PreparePaymentResponse
import org.kong.pgrouting.service.exception.PgClientException
import org.kong.pgrouting.service.exception.PgServerException
import org.kong.pgrouting.domain.payment.service.PaymentRoutingService
import org.kong.pgrouting.domain.repository.PaymentOrderRepository
import org.kong.pgrouting.domain.repository.PaymentHistoryRepository
import org.kong.pgrouting.presentation.dto.PaymentRequest
import org.kong.pgrouting.presentation.dto.PaymentResponse
import org.kong.pgrouting.service.exception.AllGatewaysFailedException
import org.kong.pgrouting.service.exception.NoAvailableGatewayException
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
            throw NoAvailableGatewayException("지원하는 결제수단이 없습니다. (입력값: ${request})")
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
            } catch (e: Exception) {
                handleAdapterFailure(
                    adapter = adapter,
                    order = order,
                    e = e,
                    request,
                )
            }
        }

        if (response == null) {
            order.status = OrderStatus.FAILED
            log.error("모든 PG사 결제 준비 실패. 요청값: $request")
            throw AllGatewaysFailedException("결제사 연동에 모두 실패했습니다. 잠시 후 다시 시도해주세요.")
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
        order: PaymentOrder,
    ) {
        val paymentHistory = PaymentHistory(
            pgName = adapter.getName(),
            status = TransactionStatus.SUCCESS,
            pgTransactionId = response.pgTransactionId
        )
        paymentHistory.paymentOrder = order
        paymentHistoryRepository.save(paymentHistory)
    }

    private fun handleAdapterFailure(
        adapter: PaymentGatewayAdapter,
        order: PaymentOrder,
        e: Exception,
        request: PaymentRequest,
    ) {
        val adapterName = adapter.getName()
        when (e) {
            is PgClientException -> {
                log.error("${adapterName}사 결제 요청 실패 (Client Error), 요청값: $request", e)
                savePaymentHistoryFail(order, adapter, TransactionStatus.FAIL_CLIENT_ERROR, e)
            }

            is PgServerException -> {
                log.warn("${adapterName}사 결제 처리 중 오류 및 Timeout 발생, 요청값: $request", e)
                savePaymentHistoryFail(order, adapter, TransactionStatus.FAIL_SERVER_ERROR, e)
            }

            is CallNotPermittedException -> {
                log.warn("${adapterName}사 오류 빈도 높으므로 사용하지 않음, 요청값: $request", e)
                savePaymentHistoryFail(order, adapter, TransactionStatus.FAIL_SERVER_ERROR, e)
            }

            else -> {
                log.error("${adapterName}사 결제 처리 중 알 수 없는 오류 발생, 요청값: $request", e)
                savePaymentHistoryFail(order, adapter, TransactionStatus.FAIL_SERVER_ERROR, e)
            }
        }
    }
}