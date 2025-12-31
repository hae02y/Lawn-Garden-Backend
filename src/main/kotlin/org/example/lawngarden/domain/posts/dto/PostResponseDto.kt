package org.example.lawngarden.domain.posts.dto

import org.example.lawngarden.domain.users.dto.UserDetailResponseDto
import java.time.LocalDate

data class PostResponseDto (
    val id: Long?,
    var createdDate: LocalDate?,
    var user: UserDetailResponseDto,
    val image : String?,
)