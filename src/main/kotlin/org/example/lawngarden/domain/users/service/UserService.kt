package org.example.lawngarden.domain.users.service

import org.example.lawngarden.common.exception.custom.EmailExistException
import org.example.lawngarden.common.exception.custom.UserExistException
import org.example.lawngarden.domain.auths.enums.LoginType
import org.example.lawngarden.domain.users.dto.RegisterRequestDto
import org.example.lawngarden.domain.users.dto.UserDetailResponseDto
import org.example.lawngarden.domain.mapper.toUser
import org.example.lawngarden.domain.mapper.toUserDetailResponseDto
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun saveUser(registerRequestDto: RegisterRequestDto) : UserDetailResponseDto {

        verifyEmail(registerRequestDto.email)
        verifyUserName(registerRequestDto.username)

        registerRequestDto.password = passwordEncoder.encode(registerRequestDto.password)
        registerRequestDto.type = LoginType.NONE
        val save : User = userRepository.save(registerRequestDto.toUser())
        return save.toUserDetailResponseDto()
    }

    fun findUser(userId: Long): UserDetailResponseDto {
        val user: User = userRepository.findByIdOrNull(userId) ?: throw RuntimeException("User with ID $userId not found");
        return user.toUserDetailResponseDto()
    }

    fun findAllUser() : List<UserDetailResponseDto> = userRepository.findAll().map{
        it.toUserDetailResponseDto()
    }

    fun findTodayCommitUser(commit: String): List<UserDetailResponseDto> {
        val userList : List<User> = when (commit) {
            "y" -> userRepository.findAllByPostCreatedDate(LocalDate.now())
            "n" -> userRepository.findUsersWithoutPostToday(LocalDate.now())
            "a" -> userRepository.findAll()
            else -> return emptyList()
        }

        return userList.map { it.toUserDetailResponseDto() }
    }

    fun verifyEmail(email: String) {
        val isExist : Boolean = userRepository.existsUserByEmail(email)
        if(isExist){
            throw EmailExistException()
        }
        else return
    }

    fun verifyUserName(userName : String){
        val existsByUsername :Boolean  = userRepository.existsByUsername(userName)
        if(existsByUsername){
            throw UserExistException()
        }
        else return
    }

}