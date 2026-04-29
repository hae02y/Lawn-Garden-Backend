package org.example.lawngarden.domain.geeknews.repository

import org.example.lawngarden.domain.geeknews.entity.GeekNewsUserState
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface GeekNewsUserStateRepository : JpaRepository<GeekNewsUserState, Long> {
    fun findAllByUserIdAndArticleIdIn(userId: Long, articleIds: List<Long>): List<GeekNewsUserState>
    fun findByUserIdAndArticleId(userId: Long, articleId: Long): GeekNewsUserState?
    fun findAllByUserIdAndBookmarkedTrueOrderByModifiedAtDesc(userId: Long, pageable: Pageable): Page<GeekNewsUserState>
}
