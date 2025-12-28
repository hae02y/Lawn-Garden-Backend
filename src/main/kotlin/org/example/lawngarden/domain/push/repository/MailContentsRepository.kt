package org.example.lawngarden.domain.push.repository

import org.example.lawngarden.domain.push.entity.MailContents
import org.springframework.data.mongodb.repository.MongoRepository

interface MailContentsRepository : MongoRepository<MailContents, String> {
}