package org.example.lawngarden.domain.push.repository

import org.example.lawngarden.domain.push.entity.MailProgress
import org.example.lawngarden.domain.users.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MailProgressRepository : JpaRepository<MailProgress, Long> {
    fun findByUser(user: User): MailProgress?
    fun findAllByIsCompletedFalse(): List<MailProgress>
}
