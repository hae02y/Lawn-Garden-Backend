package org.example.lawngarden.common.exception

import java.time.LocalDateTime

data class ApiErrorResponse(
    val errorCode: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
