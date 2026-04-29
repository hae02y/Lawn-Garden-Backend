package org.example.lawngarden.domain.geeknews.dto

import java.time.LocalDateTime

data class GeekNewsSyncLogResponseDto(
    val id: Long?,
    val requestedLimit: Int,
    val insertedCount: Int,
    val success: Boolean,
    val message: String,
    val createdAt: LocalDateTime?,
)
