package org.example.lawngarden.domain.geeknews.repository

import org.example.lawngarden.domain.geeknews.entity.GeekNewsSyncLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface GeekNewsSyncLogRepository : JpaRepository<GeekNewsSyncLog, Long> {
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<GeekNewsSyncLog>
}
