package org.example.lawngarden.domain.geeknews.dto

import java.time.LocalDateTime

data class GeekNewsStateResponseDto(
    val articleId: Long,
    val bookmarked: Boolean,
    val read: Boolean,
    val readAt: LocalDateTime?,
)
