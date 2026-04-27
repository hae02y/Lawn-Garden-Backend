package org.example.lawngarden.domain.posts.controller

import org.example.lawngarden.domain.auths.details.UserDetailsImpl
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.lawngarden.domain.posts.dto.PostDetailResponseDto
import org.example.lawngarden.domain.posts.dto.PostRequestDto
import org.example.lawngarden.domain.posts.dto.PostResponseDto
import org.example.lawngarden.domain.mapper.toPostDetailResponseDto
import org.example.lawngarden.domain.posts.service.PostService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Posts", description = "게시글 API")
class PostController(
    private val postService: PostService
) {
    //모든 포스트를 조회
    @GetMapping("")
    @Operation(summary = "게시글 목록 조회", description = "키워드로 작성자, 내용, 링크를 검색할 수 있습니다.")
    fun getAllPosts(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("keyword", required = false) keyword: String?
    ): ResponseEntity<Page<PostResponseDto>> {
        val pageData: Pageable = PageRequest.of(page, size)
        val findAllPost: Page<PostResponseDto> = postService.findAllPost(pageData, keyword)
        return ResponseEntity.ok(findAllPost)
    }

    @GetMapping("/me")
    @Operation(summary = "내 게시글 조회", description = "로그인한 사용자의 게시글만 조회합니다.")
    fun getMyPosts(
        @AuthenticationPrincipal userDetailsImpl: UserDetailsImpl,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<Page<PostResponseDto>> {
        val pageData: Pageable = PageRequest.of(page, size)
        val user = userDetailsImpl.user
        return ResponseEntity.ok(postService.findMyPosts(pageData, user))
    }

    @GetMapping("/today")
    @Operation(summary = "오늘 게시글 조회", description = "오늘 작성된 게시글을 조회합니다.")
    fun getTodayPosts(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<Page<PostResponseDto>> {
        val pageData: Pageable = PageRequest.of(page, size)
        return ResponseEntity.ok(postService.findTodayPosts(pageData))
    }

    //특정 포스트 상세 조회
    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회")
    fun getPostDetail(@PathVariable postId: Long): ResponseEntity<PostDetailResponseDto> {
        val findPostDetail: PostDetailResponseDto = postService.findPostDetail(postId)
        return ResponseEntity.ok(findPostDetail)
    }

    //포스트 등록
    @PostMapping
    @Operation(summary = "게시글 등록")
    fun postPost(
        @AuthenticationPrincipal userDetailsImpl: UserDetailsImpl,
        @ModelAttribute postRequestDto: PostRequestDto,
    ) : ResponseEntity<Void> {
        val user = userDetailsImpl.user
        val savePost = postService.savePost(postRequestDto, user)

        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(savePost.id)
            .toUri()
        return ResponseEntity.created(location).build()
    }

    //포스트 수정
    @PatchMapping("/{postId}")
    @Operation(summary = "게시글 수정")
    fun patchPost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetailsImpl: UserDetailsImpl,
        @ModelAttribute postRequestDto: PostRequestDto
    ): ResponseEntity<PostDetailResponseDto> {
        val user = userDetailsImpl.user
        val updatePost = postService.updatePost(postRequestDto, postId, user)
        return ResponseEntity.ok(updatePost.toPostDetailResponseDto())
    }

    //포스트 삭제
    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제")
    fun deletePost(
        @PathVariable postId: Long,
        @AuthenticationPrincipal userDetailsImpl: UserDetailsImpl,
    ): ResponseEntity<Void> {
        val user = userDetailsImpl.user
        postService.deletePost(postId, user)
        return ResponseEntity.noContent().build()
    }
}
