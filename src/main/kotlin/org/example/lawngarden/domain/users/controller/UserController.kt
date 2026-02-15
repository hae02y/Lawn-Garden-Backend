package org.example.lawngarden.domain.users.controller

import org.example.lawngarden.domain.users.dto.RegisterRequestDto
import org.example.lawngarden.domain.users.dto.UserDetailResponseDto
import org.example.lawngarden.domain.users.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/register")
    fun register(@RequestBody registerRequestDto : RegisterRequestDto) : ResponseEntity<UserDetailResponseDto> {
        println(registerRequestDto)
        val saveUser = userService.saveUser(registerRequestDto)
        return ResponseEntity.ok().body(saveUser)
    }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Long): ResponseEntity<UserDetailResponseDto> {
        val findUser : UserDetailResponseDto = userService.findUser(userId)
        return ResponseEntity.ok(findUser)
    }

    @GetMapping
    fun getUserList():  ResponseEntity<List<UserDetailResponseDto>> {
        val findAllUser : List<UserDetailResponseDto> = userService.findAllUser()
        return ResponseEntity.ok(findAllUser)
    }

    @GetMapping("today")
    fun getTodayUser(@RequestParam commit: String): ResponseEntity<List<UserDetailResponseDto>> {
        val findTodayCommitUser = userService.findTodayCommitUser(commit)
        return ResponseEntity.ok(findTodayCommitUser);
    }


}
