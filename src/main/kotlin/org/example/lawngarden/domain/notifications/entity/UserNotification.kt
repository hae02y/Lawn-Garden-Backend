package org.example.lawngarden.domain.notifications.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.example.lawngarden.common.entity.BaseEntity
import org.example.lawngarden.domain.notifications.enums.NotificationSeverity
import org.example.lawngarden.domain.users.entity.User
import java.time.LocalDate

@Entity
@Table(name = "user_notifications")
class UserNotification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, length = 120)
    val title: String,

    @Column(nullable = false, length = 1000)
    val message: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val severity: NotificationSeverity,

    @Column(nullable = false, length = 80)
    val code: String,

    @Column(name = "reference_date")
    val referenceDate: LocalDate? = null,

    @Column(nullable = false)
    var isRead: Boolean = false,
) : BaseEntity() {
    fun markRead() {
        isRead = true
    }
}
