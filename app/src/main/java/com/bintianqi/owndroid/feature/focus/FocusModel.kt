package com.bintianqi.owndroid.feature.focus

import kotlinx.serialization.Serializable

@Serializable
data class FocusConfig(
    val durationMinutes: Int = 25,
    val allowedPackages: List<String> = emptyList(),
    val passwordHash: String = "",
    val strictMode: Boolean = false,
    val useKioskLock: Boolean = true,
    val suspendOtherApps: Boolean = true,
    val allowNotifications: Boolean = false
)

enum class FocusState {
    IDLE,
    ACTIVE,
    COMPLETED
}

data class FocusSessionState(
    val state: FocusState = FocusState.IDLE,
    val startTimeEpochMs: Long = 0L,
    val endTimeEpochMs: Long = 0L,
    val totalSeconds: Long = 0L,
    val remainingSeconds: Long = 0L,
    val currentQuote: String = "Silence the noise. Elevate your presence.",
    val suspendedPackages: List<String> = emptyList()
)

val MINDFUL_QUOTES = listOf(
    "Silence the noise. Elevate your presence.",
    "Do one thing at a time, and do it with full attention.",
    "The discomfort of boredom is where deep focus begins.",
    "You are not missing out on anything that matters right now.",
    "Breathe. Stay anchored in the current moment.",
    "Control your attention, or algorithms will control it for you.",
    "Simplicity is the ultimate sophistication.",
    "Focus is a muscle. You are building it right now."
)
