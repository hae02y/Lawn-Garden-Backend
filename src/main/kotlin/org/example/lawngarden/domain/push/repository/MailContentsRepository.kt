package org.example.lawngarden.domain.push.repository

import org.example.lawngarden.domain.push.entity.MailContents
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MailContentsRepository : MongoRepository<MailContents, String> {
    fun findFirstByOrderByCreatedAtAsc(): MailContents?
    fun findFirstByCreatedAtAfterOrderByCreatedAtAsc(createdAt: java.time.Instant): MailContents?
}
