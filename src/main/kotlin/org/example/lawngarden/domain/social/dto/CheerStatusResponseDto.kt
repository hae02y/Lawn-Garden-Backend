package org.example.lawngarden.domain.social.dto

data class CheerStatusResponseDto(
    val canCheerToday: Boolean,
    val receivedTodayCount: Long,
    val receivedTotalCount: Long,
)
