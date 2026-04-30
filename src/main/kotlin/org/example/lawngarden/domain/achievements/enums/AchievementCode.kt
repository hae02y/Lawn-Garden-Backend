package org.example.lawngarden.domain.achievements.enums

enum class AchievementCode(
    val title: String,
    val description: String,
) {
    FIRST_WATERING("첫 물주기 달성", "처음으로 물주기 인증을 완료했습니다."),
    STREAK_7("7일 연속 인증", "7일 연속 물주기 인증을 달성했습니다."),
    LEVEL_3("Lv.3 도달", "Lv.3 성장나무 단계에 도달했습니다."),
    LEVEL_5("Lv.5 도달", "Lv.5 마스터 정원사 단계에 도달했습니다."),
    MONTHLY_12("월간 12회 인증", "이번 달 물주기 12회를 달성했습니다."),
    STREAK_30("연속 30일 인증", "30일 연속 물주기 인증을 달성했습니다."),
}
