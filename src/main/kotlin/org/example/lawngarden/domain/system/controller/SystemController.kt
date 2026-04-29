package org.example.lawngarden.domain.system.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System", description = "운영/상태 API")
class SystemController(
    @Value("\${app.version:0.0.1}")
    private val appVersion: String,
) {
    @GetMapping("/status")
    @Operation(summary = "API 상태/버전 조회")
    fun getSystemStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "version" to appVersion,
                "serverTime" to LocalDateTime.now().toString(),
                "docsUrl" to "/swagger-ui/index.html",
                "errorCodesUrl" to "/api/v1/system/error-codes",
            )
        )
    }

    @GetMapping("/error-codes")
    @Operation(summary = "표준 에러 코드 문서")
    fun getErrorCodes(): ResponseEntity<List<Map<String, String>>> {
        val payload = listOf(
            mapOf("code" to "BAD_REQUEST", "httpStatus" to "400", "description" to "요청 값이 잘못되었습니다."),
            mapOf("code" to "UNAUTHORIZED", "httpStatus" to "401", "description" to "인증이 필요하거나 토큰이 유효하지 않습니다."),
            mapOf("code" to "FORBIDDEN", "httpStatus" to "403", "description" to "권한이 없습니다."),
            mapOf("code" to "NOT_FOUND", "httpStatus" to "404", "description" to "요청한 리소스를 찾을 수 없습니다."),
            mapOf("code" to "CONFLICT", "httpStatus" to "409", "description" to "중복/충돌 상태입니다."),
            mapOf("code" to "INTERNAL_SERVER_ERROR", "httpStatus" to "500", "description" to "서버 내부 오류가 발생했습니다."),
        )
        return ResponseEntity.ok(payload)
    }
}
