package com.kong.pgrouting.domain.repository

import org.kong.pgrouting.domain.entity.PaymentOrder
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentOrderRepository : JpaRepository<PaymentOrder, Long> {
}