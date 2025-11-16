package com.kong.pgrouting

import com.kong.pgrouting.config.PaymentProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@EnableConfigurationProperties(PaymentProperties::class)
@SpringBootApplication
class PgRoutingApplication

fun main(args: Array<String>) {
    runApplication<PgRoutingApplication>(*args)
}
