package org.example.lawngarden.domain.geeknews.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import org.example.lawngarden.domain.geeknews.dto.GeekNewsListResponseDto
import org.example.lawngarden.domain.geeknews.dto.GeekNewsResponseDto
import org.example.lawngarden.domain.geeknews.dto.GeekNewsStateResponseDto
import org.example.lawngarden.domain.geeknews.dto.GeekNewsSyncResponseDto
import org.example.lawngarden.domain.geeknews.service.GeekNewsService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.core.annotation.AuthenticationPrincipal

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
        @AuthenticationPrincipal userDetails: UserDetailsImpl?,
    ): ResponseEntity<GeekNewsListResponseDto> {
        val pageable: Pageable = PageRequest.of(page, size)
        val resultPage: Page<GeekNewsResponseDto> = geekNewsService.getGeekNews(pageable, keyword, userDetails?.user)

        val response = GeekNewsListResponseDto(
            items = resultPage.content,
            page = resultPage.number,
            size = resultPage.size,
            totalElements = resultPage.totalElements,
            totalPages = resultPage.totalPages,
            hasNext = resultPage.hasNext(),
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/sync")
    @Operation(summary = "GeekNews RSS 동기화")
    fun syncGeekNews(
        @RequestParam("limit", defaultValue = "50") limit: Int,
    ): ResponseEntity<GeekNewsSyncResponseDto> {
        val inserted = geekNewsService.syncGeekNews(limit)
        return ResponseEntity.ok(GeekNewsSyncResponseDto(inserted = inserted, requestedLimit = limit))
    }

    @GetMapping("/bookmarks/me")
    @Operation(summary = "내 북마크 GeekNews 조회")
    fun getMyBookmarkedGeekNews(
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int,
    ): ResponseEntity<GeekNewsListResponseDto> {
        val pageable: Pageable = PageRequest.of(page, size)
        return ResponseEntity.ok(geekNewsService.getMyBookmarkedGeekNews(pageable, userDetails.user))
    }

    @PostMapping("/{articleId}/bookmark")
    @Operation(summary = "GeekNews 북마크 토글")
    fun toggleBookmark(
        @PathVariable articleId: Long,
        @RequestParam("bookmarked", defaultValue = "true") bookmarked: Boolean,
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<GeekNewsStateResponseDto> {
        return ResponseEntity.ok(geekNewsService.toggleBookmark(articleId, bookmarked, userDetails.user))
    }

    @PostMapping("/{articleId}/read")
    @Operation(summary = "GeekNews 읽음 처리")
    fun markRead(
        @PathVariable articleId: Long,
        @AuthenticationPrincipal userDetails: UserDetailsImpl,
    ): ResponseEntity<GeekNewsStateResponseDto> {
        return ResponseEntity.ok(geekNewsService.markRead(articleId, userDetails.user))
    }
}
