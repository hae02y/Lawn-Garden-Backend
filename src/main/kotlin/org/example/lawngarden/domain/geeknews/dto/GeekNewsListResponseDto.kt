package org.example.lawngarden.domain.geeknews.dto

data class GeekNewsListResponseDto(
    val items: List<GeekNewsResponseDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
