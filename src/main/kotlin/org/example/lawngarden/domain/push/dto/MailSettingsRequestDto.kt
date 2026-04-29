package org.example.lawngarden.domain.push.dto

import org.example.lawngarden.domain.push.enums.MailCategory
import org.example.lawngarden.domain.push.enums.MailStatus

data class MailSettingsRequestDto(
    val status: MailStatus,
    val preferredDays: Set<String>,
    val preferredHour: Int,
    val categories: Set<MailCategory>,
)
