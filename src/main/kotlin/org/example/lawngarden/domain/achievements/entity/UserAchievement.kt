package org.example.lawngarden.domain.achievements.entity

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
import jakarta.persistence.UniqueConstraint
import org.example.lawngarden.common.entity.BaseEntity
import org.example.lawngarden.domain.achievements.enums.AchievementCode
import org.example.lawngarden.domain.users.entity.User
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_achievements",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "code"])],
)
class UserAchievement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    val code: AchievementCode,

    @Column(nullable = false)
    val unlockedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity()
