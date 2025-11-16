package org.kong.pgrouting.service.exception

class PgServerException(message: String, cause: Throwable? = null): PaymentException(message, cause)