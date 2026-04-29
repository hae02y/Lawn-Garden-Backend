package org.example.lawngarden.domain.push.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.example.lawngarden.common.entity.BaseEntity
import org.example.lawngarden.domain.push.enums.MailCategory
import org.example.lawngarden.domain.push.enums.MailStatus
import org.example.lawngarden.domain.users.entity.User
import java.time.DayOfWeek

@Entity
@Table(name = "mails")
class Mail(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    var status: MailStatus = MailStatus.ON,

    var preferredHour: Int? = 9,

    var preferredDays: String? = defaultDaysString(),

    var categories: String? = defaultCategoriesString(),

) : BaseEntity() {
    fun changeStatus(newStatus: MailStatus) {
        if (this.status == newStatus) return
        this.status = newStatus
    }

    fun updateSettings(
        newStatus: MailStatus,
        newPreferredHour: Int,
        newPreferredDays: Set<DayOfWeek>,
        newCategories: Set<MailCategory>,
    ) {
        status = newStatus
        preferredHour = newPreferredHour
        preferredDays = toDaysString(newPreferredDays)
        categories = toCategoriesString(newCategories)
    }

    fun getPreferredDays(): Set<DayOfWeek> =
        preferredDays.orEmpty()
            .split(',')
            .mapNotNull { runCatching { DayOfWeek.valueOf(it.trim()) }.getOrNull() }
            .toSet()
            .ifEmpty { defaultDays() }

    fun getCategories(): Set<MailCategory> =
        categories.orEmpty()
            .split(',')
            .mapNotNull { runCatching { MailCategory.valueOf(it.trim()) }.getOrNull() }
            .toSet()
            .ifEmpty { defaultCategories() }

    companion object {
        private fun defaultDays(): Set<DayOfWeek> = DayOfWeek.entries.toSet()
        private fun defaultCategories(): Set<MailCategory> = setOf(MailCategory.BACKEND, MailCategory.FRONTEND, MailCategory.AI)

        fun defaultDaysString(): String = toDaysString(defaultDays())
        fun defaultCategoriesString(): String = toCategoriesString(defaultCategories())

        private fun toDaysString(values: Set<DayOfWeek>): String =
            values.sortedBy { it.value }.joinToString(",") { it.name }

        private fun toCategoriesString(values: Set<MailCategory>): String =
            values.sortedBy { it.name }.joinToString(",") { it.name }
    }
}
