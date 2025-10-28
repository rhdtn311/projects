package org.example.pgrouting.for_test

import org.springframework.data.jpa.repository.JpaRepository

interface TestRepository: JpaRepository<TestEntity, String> {
}