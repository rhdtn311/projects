package org.example.pgrouting.domain.repository

import org.example.pgrouting.domain.entity.PaymentHistory
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentHistoryRepository : JpaRepository<PaymentHistory, Long>