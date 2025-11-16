package org.kong.pgrouting.service.exception

abstract class PaymentException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)