package com.kong.pgrouting.for_test

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TestConfigService(
    private val testRepository: TestRepository
) {
    @Transactional
    fun updateConfig(dto: FailureConfigDto) {
        val entity = TestEntity(
            id = dto.pgName,
            isSystemFailure = dto.isSystemFailure,
            isTimeout = dto.isTimeout
        )
        testRepository.save(entity)
    }
}