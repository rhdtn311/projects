package org.example.pgrouting.for_test

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TestConfigController(
    private val testConfigService: TestConfigService
) {
    @PostMapping("/test/config")
    fun updateFailureConfig(@RequestBody dto: FailureConfigDto): ResponseEntity<String> {
        testConfigService.updateConfig(dto)
        return ResponseEntity.ok("Config updated for ${dto.pgName}: 5xx=${dto.isSystemFailure}, Timeout=${dto.isTimeout}")
    }
}