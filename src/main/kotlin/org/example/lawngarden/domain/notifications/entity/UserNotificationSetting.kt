package org.example.lawngarden.domain.notifications.entity

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

@Entity
@Table(name = "user_notification_settings")
class UserNotificationSetting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    @Column(nullable = false)
    var missionEnabled: Boolean = true,

    @Column(nullable = false)
    var streakRiskEnabled: Boolean = true,

    @Column(nullable = false)
    var levelUpEnabled: Boolean = true,

    @Column(nullable = false)
    var rewardEnabled: Boolean = true,

    @Column(nullable = false)
    var quietHoursEnabled: Boolean = false,

    @Column(nullable = false)
    var quietStartHour: Int = 23,

    @Column(nullable = false)
    var quietEndHour: Int = 8,
) : BaseEntity() {
    fun update(
        missionEnabled: Boolean,
        streakRiskEnabled: Boolean,
        levelUpEnabled: Boolean,
        rewardEnabled: Boolean,
        quietHoursEnabled: Boolean,
        quietStartHour: Int,
        quietEndHour: Int,
    ) {
        this.missionEnabled = missionEnabled
        this.streakRiskEnabled = streakRiskEnabled
        this.levelUpEnabled = levelUpEnabled
        this.rewardEnabled = rewardEnabled
        this.quietHoursEnabled = quietHoursEnabled
        this.quietStartHour = quietStartHour.coerceIn(0, 23)
        this.quietEndHour = quietEndHour.coerceIn(0, 23)
    }
}
