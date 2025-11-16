package org.kong.pgrouting.test

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class TestEntity(
    @Id
    val id: String? = null,
    val isSystemFailure: Boolean = false,
    val isTimeout: Boolean = false,
)