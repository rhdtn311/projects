package com.kong.pgrouting.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "payment")
data class PaymentProperties(
    val timeoutSeconds: Int,
    val webhookUrl: String,
    val gateways: Map<String, GatewayProperties> = emptyMap()
)

data class GatewayProperties(
    val name: String,
    val apiKey: String,
    val supports: List<String> = emptyList(),
    val fees: Map<String, Double> = emptyMap()
)