package com.kong.pgrouting.for_test

data class FailureConfigDto(
    val pgName: String,
    val isSystemFailure: Boolean = false,
    val isTimeout: Boolean = false
)