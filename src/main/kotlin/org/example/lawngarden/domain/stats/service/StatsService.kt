package org.example.lawngarden.domain.stats.service

import jakarta.persistence.Tuple
import org.example.lawngarden.domain.posts.repository.PostRepository
import org.example.lawngarden.domain.users.dto.UserStatsResponseDto
import org.example.lawngarden.domain.users.entity.User
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import org.example.lawngarden.domain.mapper.toUserStatsResponseDto


@Service
class StatsService(
    private val postRepository: PostRepository
) {

    private fun Tuple.toUserAndPostCount(): Pair<User, Long> {
        val user = this["user"] as User
        val postCount = (this["postCount"] as Number).toLong()
        return user to postCount
    }


    fun getWeeklyStats() : List<UserStatsResponseDto> {

        val today : LocalDate = LocalDate.now()

        val monday : LocalDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday : LocalDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val postList : List<Tuple> = postRepository.findAllByCreatedDateBetween(monday, sunday)
        val pairs = postList.map { tuple -> tuple.toUserAndPostCount() }
        val toList : List<UserStatsResponseDto> = pairs.stream().map { x -> x.first.toUserStatsResponseDto(x.second) }.toList()
        return toList;
    }

    fun getTodayStats(): List<UserStatsResponseDto> {
        val postList: List<Tuple> = postRepository.findAllByCreatedDateBetween(LocalDate.now(), LocalDate.now())
        val pairs = postList.map { tuple -> tuple.toUserAndPostCount() }
        return pairs.map { it.first.toUserStatsResponseDto(it.second) }
    }
}
