package com.example.cyberquiz.model

const val DEFAULT_EXAM_TIME_LIMIT_MINUTES = 20

val EXAM_TIME_LIMIT_OPTIONS: List<Int> = listOf(
    5, 10, 20, 30, 45, 60, 90, 120
)

fun suggestedExamTimeLimitMinutes(questionCount: Int): Int = when {
    questionCount <= 5 -> 5
    questionCount <= 10 -> 10
    questionCount <= 20 -> 20
    questionCount <= 50 -> 45
    questionCount <= 100 -> 90
    else -> 120
}

fun examDeadlineEpochMs(
    startedAtEpochMs: Long,
    timeLimitMinutes: Int
): Long? {
    if (startedAtEpochMs <= 0L || timeLimitMinutes <= 0) return null
    val durationMs = timeLimitMinutes.toLong() * 60_000L
    return if (Long.MAX_VALUE - startedAtEpochMs < durationMs) {
        Long.MAX_VALUE
    } else {
        startedAtEpochMs + durationMs
    }
}
fun examRemainingMillis(
    deadlineEpochMs: Long?,
    nowEpochMs: Long = System.currentTimeMillis()
): Long = deadlineEpochMs
    ?.minus(nowEpochMs)
    ?.coerceAtLeast(0L)
    ?: 0L

fun formatExamRemainingTime(remainingMillis: Long): String {
    val totalSeconds = ((remainingMillis.coerceAtLeast(0L) + 999L) / 1_000L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
