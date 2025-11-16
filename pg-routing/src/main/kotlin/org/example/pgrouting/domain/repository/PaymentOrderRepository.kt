package org.example.pgrouting.domain.repository

import org.example.pgrouting.domain.entity.PaymentOrder
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentOrderRepository : JpaRepository<PaymentOrder, Long> {
}