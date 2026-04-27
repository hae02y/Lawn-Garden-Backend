package org.example.lawngarden.domain.push.dto

import org.example.lawngarden.domain.push.enums.MailStatus

data class MailStatusResponseDto(
    val status: MailStatus,
)
