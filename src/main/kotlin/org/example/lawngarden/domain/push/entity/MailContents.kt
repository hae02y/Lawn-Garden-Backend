package org.example.lawngarden.domain.push.entity

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.example.lawngarden.common.entity.BaseEntity
import org.example.lawngarden.domain.push.enums.MailCategory
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "mail_contents")
class MailContents(

    @Id
    val id : String? = null,

    var name : String,

    var content : String,

    var category: MailCategory = MailCategory.NONE,

    @Field(name = "siteLink")
    var siteLink: String,

    @CreatedDate
    val createdAt: Instant? = null,

    @LastModifiedDate
    val updatedAt: Instant? = null,
    )