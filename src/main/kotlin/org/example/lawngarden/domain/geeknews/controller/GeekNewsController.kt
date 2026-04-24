package org.example.lawngarden.domain.geeknews.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.geeknews.dto.GeekNewsResponseDto
import org.example.lawngarden.domain.geeknews.service.GeekNewsService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/geeknews")
@Tag(name = "GeekNews", description = "GeekNews 수집/조회 API")
class GeekNewsController(
    private val geekNewsService: GeekNewsService,
) {
    @GetMapping
    @Operation(summary = "GeekNews 목록 조회")
    fun getGeekNews(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int,
        @RequestParam("keyword", required = false) keyword: String?,
    ): ResponseEntity<Page<GeekNewsResponseDto>> {
        val pageable: Pageable = PageRequest.of(page, size)
        return ResponseEntity.ok(geekNewsService.getGeekNews(pageable, keyword))
    }

    @PostMapping("/sync")
    @Operation(summary = "GeekNews RSS 동기화")
    fun syncGeekNews(
        @RequestParam("limit", defaultValue = "50") limit: Int,
    ): ResponseEntity<Map<String, Any>> {
        val inserted = geekNewsService.syncGeekNews(limit)
        return ResponseEntity.ok(mapOf("inserted" to inserted))
    }
}
