package org.example.lawngarden.domain.push.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.example.lawngarden.common.entity.BaseEntity
import org.example.lawngarden.domain.users.entity.User
import java.time.LocalDateTime

@Entity
@Table(name = "mail_progress")
class MailProgress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    var user: User,

    @Column(name = "last_sent_mail_content_id")
    var lastSentMailContentId: String? = null,

    @Column(name = "last_sent_at")
    var lastSentAt: LocalDateTime? = null,

    @Column(name = "is_completed", nullable = false)
    var isCompleted: Boolean = false,
) : BaseEntity()
