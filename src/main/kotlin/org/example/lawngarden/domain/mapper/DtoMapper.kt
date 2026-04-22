package org.example.lawngarden.domain.mapper

import org.example.lawngarden.domain.auths.enums.Role
import org.example.lawngarden.domain.posts.dto.PostDetailResponseDto
import org.example.lawngarden.domain.posts.dto.PostRequestDto
import org.example.lawngarden.domain.posts.dto.PostResponseDto
import org.example.lawngarden.domain.posts.entity.Post
import org.example.lawngarden.domain.users.dto.RegisterRequestDto
import org.example.lawngarden.domain.users.dto.UserDetailResponseDto
import org.example.lawngarden.domain.users.dto.UserStatsResponseDto
import org.example.lawngarden.domain.users.entity.User
import java.time.LocalDate
import java.util.*


fun RegisterRequestDto.toUser(): User = User(
    username = this.username,
    password = this.password,
    email = this.email,
    id = null,
    role = Role.USER,
    type = this.type,
    post = mutableListOf()
)

fun User.toUserDetailResponseDto(): UserDetailResponseDto = UserDetailResponseDto(
    id = this.id,
    username = this.username,
    email = this.email,
)

fun User.toUserStatsResponseDto(count : Long): UserStatsResponseDto = UserStatsResponseDto(
    id = this.id,
    username = this.username,
    email = this.email,
    commitCount = count.toString()
)

fun Post.toPostResponseDto() : PostResponseDto = PostResponseDto(
        id = this.id,
        createdDate = this.createdDate,
        user = this.user.toUserDetailResponseDto(),
        image = this.image,
)

fun Post.toPostDetailResponseDto() : PostDetailResponseDto = PostDetailResponseDto(
    id = this.id,
    createdDate = this.createdDate,
    user = this.user.toUserDetailResponseDto(),
    link = this.link,
    contents = this.contents,
    image = this.image
)


fun PostRequestDto.toPost(user: User, imageName: String?): Post {
    return Post(
        id = null,
        link = this.link,
        contents = this.contents,
        image = imageName, // 변환 핵심!
        user = user
    )
}

fun Post.updatePost(postRequestDto: PostRequestDto, imageName: String?) : Post {
    return Post(
        id = this.id,
        user = this.user,
        link = postRequestDto.link,
        contents = postRequestDto.contents,
        image = imageName,
    )
}

