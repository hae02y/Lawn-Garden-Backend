package org.example.lawngarden.domain.posts.service

import org.example.lawngarden.domain.images.service.ImageService
import org.example.lawngarden.domain.mapper.toPost
import org.example.lawngarden.domain.mapper.toPostDetailResponseDto
import org.example.lawngarden.domain.mapper.toPostResponseDto
import org.example.lawngarden.domain.mapper.updatePost
import org.example.lawngarden.domain.posts.dto.PostDetailResponseDto
import org.example.lawngarden.domain.posts.dto.PostRequestDto
import org.example.lawngarden.domain.posts.dto.PostResponseDto
import org.example.lawngarden.domain.posts.entity.Post
import org.example.lawngarden.domain.posts.repository.PostRepository
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.service.UserLevelService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.NoSuchElementException

@Service
class PostService(
    private val postRepository: PostRepository,
    private val imageService: ImageService,
    private val userLevelService: UserLevelService,
) {
    companion object {
        private const val MAX_IMAGE_SIZE_BYTES = 3 * 1024 * 1024L
    }

    fun findAllPost(pageData: Pageable, keyword: String?): Page<PostResponseDto> {
        val findAll: Page<Post> =
            if (keyword.isNullOrBlank()) postRepository.findAllByOrderByCreatedDateDescIdDesc(pageData)
            else postRepository.searchByKeyword(keyword, pageData)

        return findAll.map { it.toPostResponseDto() }
    }

    fun findMyPosts(pageData: Pageable, user: User): Page<PostResponseDto> {
        return postRepository.findAllByUserOrderByCreatedDateDescIdDesc(user, pageData)
            .map { it.toPostResponseDto() }
    }

    fun findTodayPosts(pageData: Pageable): Page<PostResponseDto> {
        return postRepository.findAllByCreatedDateOrderByCreatedDateDescIdDesc(LocalDate.now(), pageData)
            .map { it.toPostResponseDto() }
    }

    fun findPostDetail(postId: Long): PostDetailResponseDto {
        val findById: Post = postRepository.findByIdOrNull(postId)
            ?: throw NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다. id=$postId")
        val postDetailResponseDto: PostDetailResponseDto = findById.toPostDetailResponseDto()
        return postDetailResponseDto
    }

    @Transactional
    fun savePost(post: PostRequestDto, user: User): Post {
        val imageFile = post.imageFile ?: throw IllegalArgumentException("이미지는 필수입니다.")
        if (imageFile.isEmpty) throw IllegalArgumentException("이미지는 필수입니다.")
        if (postRepository.existsPostByUserAndCreatedDate(user, LocalDate.now())) {
            throw IllegalArgumentException("이미 등록된 Post가 있습니다.")
        }
        validateImageFile(imageFile)
        val imageName = imageService.upload(imageFile)

        val savedPost = postRepository.save(post.toPost(user, imageName))
        userLevelService.syncUserLevel(user.id)
        return savedPost
    }

    @Transactional
    fun updatePost(post: PostRequestDto, postId: Long, user: User): Post {
        val findPostById = postRepository.findPostById(postId) ?: throw NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다. id=$postId")
        if (findPostById.user.id != user.id) throw AccessDeniedException("게시글 수정 권한이 없습니다.")

        val newImageFile = post.imageFile
        val imageName = if (newImageFile != null && !newImageFile.isEmpty) {
            validateImageFile(newImageFile)
            imageService.upload(newImageFile)
        } else {
            findPostById.image
        }

        return postRepository.save(findPostById.updatePost(post, imageName))
    }

    fun deletePost(postId: Long, user: User) {
        val findById: Post =
            postRepository.findPostById(postId) ?: throw NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다. id=$postId")

        if (findById.user.id != user.id) {
            throw AccessDeniedException("게시글 삭제 권한이 없습니다.")
        }
        postRepository.delete(findById)
        userLevelService.syncUserLevel(user.id)
    }

    private fun validateImageFile(imageFile: MultipartFile) {
        if (!imageFile.contentType.orEmpty().startsWith("image/")) {
            throw IllegalArgumentException("이미지형식만 업로드할 수 있습니다.")
        }
        if (imageFile.size > MAX_IMAGE_SIZE_BYTES) {
            throw IllegalArgumentException("파일 용량이 너무 큽니다.(최대 3MB)")
        }
    }
}
