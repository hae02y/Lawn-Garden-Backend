package org.example.lawngarden.domain.geeknews.dto

import java.time.LocalDateTime

data class GeekNewsResponseDto(
    val id: Long?,
    val sourceId: String,
    val title: String,
    val link: String,
    val summary: String?,
    val publishedAt: LocalDateTime?,
)
