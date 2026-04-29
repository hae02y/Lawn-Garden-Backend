package org.example.lawngarden.domain.users.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import org.example.lawngarden.domain.mapper.toUserDetailResponseDto
import org.example.lawngarden.domain.users.dto.RegisterRequestDto
import org.example.lawngarden.domain.users.dto.UserDetailResponseDto
import org.example.lawngarden.domain.users.dto.UserLevelHistoryResponseDto
import org.example.lawngarden.domain.users.dto.UserLevelProgressResponseDto
import org.example.lawngarden.domain.users.service.UserService
import org.example.lawngarden.domain.users.service.UserLevelService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.NoSuchElementException

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "사용자 API")
class UserController(
    private val userService: UserService,
    private val userLevelService: UserLevelService,
) {
    @PostMapping("/register")
    @Operation(summary = "회원가입")
    fun register(@RequestBody registerRequestDto : RegisterRequestDto) : ResponseEntity<UserDetailResponseDto> {
        val saveUser = userService.saveUser(registerRequestDto)
        return ResponseEntity.ok().body(saveUser)
    }

    @GetMapping("/{userId}")
    @Operation(summary = "사용자 상세 조회")
    fun getUser(@PathVariable userId: Long): ResponseEntity<UserDetailResponseDto> {
        val findUser : UserDetailResponseDto = userService.findUser(userId)
        return ResponseEntity.ok(findUser)
    }

    @GetMapping
    @Operation(summary = "사용자 목록 조회")
    fun getUserList():  ResponseEntity<List<UserDetailResponseDto>> {
        val findAllUser : List<UserDetailResponseDto> = userService.findAllUser()
        return ResponseEntity.ok(findAllUser)
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    fun getMyUser(
        @AuthenticationPrincipal userDetailsImpl: UserDetailsImpl,
    ): ResponseEntity<UserDetailResponseDto> {
        return ResponseEntity.ok(userDetailsImpl.user.toUserDetailResponseDto())
    }

    @GetMapping("/today")
    @Operation(summary = "오늘 커밋 사용자 조회")
    fun getTodayUser(@RequestParam commit: String): ResponseEntity<List<UserDetailResponseDto>> {
        val findTodayCommitUser = userService.findTodayCommitUser(commit)
        return ResponseEntity.ok(findTodayCommitUser)
    }

    @GetMapping("/me/level-progress")
    @Operation(summary = "내 레벨 진행도 조회")
    fun getMyLevelProgress(
        @AuthenticationPrincipal userDetailsImpl: UserDetailsImpl,
    ): ResponseEntity<UserLevelProgressResponseDto> {
        val userId = userDetailsImpl.user.id ?: throw NoSuchElementException("User id not found")
        return ResponseEntity.ok(userLevelService.getLevelProgress(userId))
    }

    @GetMapping("/me/level-history")
    @Operation(summary = "내 레벨업 이력 조회")
    fun getMyLevelHistory(
        @AuthenticationPrincipal userDetailsImpl: UserDetailsImpl,
        @RequestParam("size", defaultValue = "20") size: Int,
    ): ResponseEntity<List<UserLevelHistoryResponseDto>> {
        val userId = userDetailsImpl.user.id ?: throw NoSuchElementException("User id not found")
        return ResponseEntity.ok(userLevelService.getLevelHistories(userId, size))
    }


}
