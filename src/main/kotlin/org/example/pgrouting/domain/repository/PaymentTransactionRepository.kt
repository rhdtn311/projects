package org.example.pgrouting.domain.repository

import org.example.pgrouting.domain.entity.PaymentTransaction
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentTransactionRepository : JpaRepository<PaymentTransaction, Long>