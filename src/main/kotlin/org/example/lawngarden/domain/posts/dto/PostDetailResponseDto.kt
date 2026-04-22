package org.example.lawngarden.domain.posts.dto

import org.example.lawngarden.domain.users.dto.UserDetailResponseDto
import java.time.LocalDate

data class PostDetailResponseDto (
    val id: Long?,
    var link: String?,
    var createdDate: LocalDate?,
    var user: UserDetailResponseDto?,
    var contents : String?,
    var image: String?
)