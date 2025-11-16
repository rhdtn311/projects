package org.kong.pgrouting.domain.repository

import org.kong.pgrouting.domain.entity.PaymentHistory
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentHistoryRepository : JpaRepository<PaymentHistory, Long>