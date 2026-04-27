package org.example.lawngarden.domain.users.enums

enum class UserLevel(
    val level: Long,
    val minPostCount: Long,
    val displayName: String,
) {
    SPROUT(1, 0, "처음 심은 새싹"),
    SAPLING(2, 7, "쑥쑥 자라는 새싹나무"),
    GROWING_TREE(3, 15, "든든한 중간나무"),
    BIG_TREE(4, 30, "무성한 큰나무"),
    MASTER_GARDENER(5, 60, "전설의 정원사");

    companion object {
        fun fromPostCount(postCount: Long): UserLevel =
            entries
                .filter { postCount >= it.minPostCount }
                .maxByOrNull { it.level }
                ?: SPROUT

        fun fromLevel(level: Long?): UserLevel =
            entries.firstOrNull { it.level == level } ?: SPROUT
    }
}
