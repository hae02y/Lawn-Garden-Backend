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
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.AccessDeniedException
import java.time.LocalDate
import java.util.NoSuchElementException

@Service
class PostService(
    private val postRepository: PostRepository,
    private val imageService: ImageService,
) {

    fun findAllPost(pageData: Pageable, keyword: String): Page<PostResponseDto> {

        val findAll: Page<Post> =
            if (keyword.isEmpty()) postRepository.findAllByOrderByCreatedDateDescIdDesc(pageData)
             else postRepository.findAllByUserUsernameContainingOrderByCreatedDateDescIdDesc(keyword, pageData)

        return findAll.map { x -> x?.toPostResponseDto() }
    }

    fun findPostDetail(postId: Long): PostDetailResponseDto {
        val findById: Post = postRepository.findByIdOrNull(postId) ?: throw RuntimeException("post가 없습니다.")
        val postDetailResponseDto: PostDetailResponseDto = findById.toPostDetailResponseDto()
        return postDetailResponseDto
    }

    @Transactional
    fun savePost(post: PostRequestDto, user: User): Post {

        val maxSize = 3 * 1024 * 1024

        if (post.link == null || post.imageFile!!.isEmpty) throw IllegalArgumentException("이미지는 필수입니다.")
        if (postRepository.existsPostByUserAndCreatedDate(user, LocalDate.now())) throw RuntimeException("이미 등록된 Post가 있습니다.")
        if (!post.imageFile!!.contentType!!.startsWith("image/")) throw IllegalArgumentException("이미지형식만 업로드할 수 있습니다.")
        if (post.imageFile!!.size > maxSize) throw IllegalArgumentException("파일 용량이 너무 큽니다.(최대 3MB)")
        val imageName = imageService.upload(post.imageFile!!)

        return postRepository.save(post.toPost(user, imageName))
    }

    @Transactional
    fun updatePost(post: PostRequestDto, postId: Long, user: User): Post {
        val findPostById = postRepository.findPostById(postId) ?: throw NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다. id=$postId")
        if (findPostById.user.id != user.id)  throw AccessDeniedException("유저가 없습니다.")

        val imageName = if(post.imageFile != null) imageService.upload(post.imageFile!!)
                        else findPostById.image

        return postRepository.save(findPostById.updatePost(post, imageName))
    }

    fun deletePost(postId: Long, user: User) {
        val findById: Post =
            postRepository.findPostById(postId) ?: throw NoSuchElementException("해당 ID의 게시글이 존재하지 않습니다. id=$postId")

        if (findById.user.id != user.id) {
            throw AccessDeniedException("유저가 일치하지 않습니다.")
        }
        postRepository.delete(findById)
    }
}