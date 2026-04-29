package org.example.lawngarden.domain.users.repository

import org.example.lawngarden.domain.users.entity.UserLevelHistory
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface UserLevelHistoryRepository : JpaRepository<UserLevelHistory, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): List<UserLevelHistory>
}
