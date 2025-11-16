package org.kong.pgrouting.domain.payment.exception

import java.io.IOException

class PgServerException(message: String, cause: Throwable? = null) : IOException(message, cause)