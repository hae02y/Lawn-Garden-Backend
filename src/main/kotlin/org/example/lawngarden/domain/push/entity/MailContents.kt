package org.example.lawngarden.domain.push.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.example.lawngarden.common.entity.BaseEntity
import org.example.lawngarden.domain.push.enums.MailCategory

@Entity
@Table(name = "mail_contents")
class MailContents(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long,

    var name : String,

    var content : String,

    var category: MailCategory = MailCategory.NONE,

    @Column(name = "site_link")
    var siteLink: String,
    ) : BaseEntity()