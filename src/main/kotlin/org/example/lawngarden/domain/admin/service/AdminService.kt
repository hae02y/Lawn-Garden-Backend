package org.example.lawngarden.domain.admin.service

import org.example.lawngarden.domain.admin.dto.AdminSyncResponseDto
import org.example.lawngarden.domain.auths.enums.Role
import org.example.lawngarden.domain.geeknews.dto.GeekNewsSyncLogResponseDto
import org.example.lawngarden.domain.geeknews.entity.GeekNewsSyncLog
import org.example.lawngarden.domain.geeknews.repository.GeekNewsSyncLogRepository
import org.example.lawngarden.domain.geeknews.service.GeekNewsService
import org.example.lawngarden.domain.users.entity.User
import org.example.lawngarden.domain.users.service.UserLevelService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val geekNewsService: GeekNewsService,
    private val geekNewsSyncLogRepository: GeekNewsSyncLogRepository,
    private val userLevelService: UserLevelService,
) {
    @Transactional
    fun syncGeekNews(limit: Int, requester: User): AdminSyncResponseDto {
        validateAdmin(requester)

        val normalizedLimit = limit.coerceIn(1, 300)
        return runCatching {
            val insertedCount = geekNewsService.syncGeekNews(normalizedLimit)
            geekNewsSyncLogRepository.save(
                GeekNewsSyncLog(
                    requestedLimit = normalizedLimit,
                    insertedCount = insertedCount,
                    success = true,
                    message = "manual sync succeeded",
                )
            )
            AdminSyncResponseDto(true, "GeekNews 동기화 성공", insertedCount)
        }.getOrElse { ex ->
            geekNewsSyncLogRepository.save(
                GeekNewsSyncLog(
                    requestedLimit = normalizedLimit,
                    insertedCount = 0,
                    success = false,
                    message = ex.message ?: "manual sync failed",
                )
            )
            throw IllegalStateException("GeekNews 동기화 실패: ${ex.message}")
        }
    }

    @Transactional(readOnly = true)
    fun getGeekNewsSyncLogs(page: Int, size: Int, requester: User): Page<GeekNewsSyncLogResponseDto> {
        validateAdmin(requester)
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 50))
        return geekNewsSyncLogRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map {
                GeekNewsSyncLogResponseDto(
                    id = it.id,
                    requestedLimit = it.requestedLimit,
                    insertedCount = it.insertedCount,
                    success = it.success,
                    message = it.message,
                    createdAt = it.createdAt,
                )
            }
    }

    @Transactional
    fun syncUserLevels(requester: User): AdminSyncResponseDto {
        validateAdmin(requester)
        val updated = userLevelService.syncAllUserLevels()
        return AdminSyncResponseDto(
            success = true,
            message = "사용자 레벨 동기화 완료",
            affectedCount = updated,
        )
    }

    private fun validateAdmin(user: User) {
        if (user.getRole() != Role.ADMIN) {
            throw AccessDeniedException("관리자 권한이 필요합니다.")
        }
    }
}
