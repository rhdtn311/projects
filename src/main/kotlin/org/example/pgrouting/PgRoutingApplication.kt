package org.example.pgrouting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PgRoutingApplication

fun main(args: Array<String>) {
    runApplication<PgRoutingApplication>(*args)
}
