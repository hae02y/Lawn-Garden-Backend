package org.example.lawngarden.domain.social.repository

import org.example.lawngarden.domain.social.entity.GardenCheer
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface GardenCheerRepository : JpaRepository<GardenCheer, Long> {
    fun existsByFromUserIdAndToUserIdAndCheerDate(fromUserId: Long, toUserId: Long, cheerDate: LocalDate): Boolean
    fun countByToUserIdAndCheerDate(toUserId: Long, cheerDate: LocalDate): Long
    fun countByToUserId(toUserId: Long): Long
}
