package org.example.lawngarden.domain.users.dto

import org.example.lawngarden.domain.auths.enums.LoginType

data class RegisterRequestDto(
    var username: String,
    var password: String?,
    var email: String,
    var type: LoginType = LoginType.NONE,
)