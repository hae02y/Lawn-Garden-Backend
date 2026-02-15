package org.example.lawngarden.domain.posts.dto

import org.springframework.web.multipart.MultipartFile

data class PostRequestDto(
    var link: String? = null,
    var contents: String? = null,
    var imageFile: MultipartFile? = null,
)