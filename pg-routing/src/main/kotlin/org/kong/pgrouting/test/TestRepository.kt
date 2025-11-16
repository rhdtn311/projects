package org.kong.pgrouting.test

import org.springframework.data.jpa.repository.JpaRepository

interface TestRepository: JpaRepository<TestEntity, String> {
}