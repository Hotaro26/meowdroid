package com.hotaro.meowdroid.feature.focus

import kotlinx.serialization.Serializable

enum class FocusExitKeyType {
    NONE, PASSWORD, QR_CODE
}

@Serializable
data class AppBag(
    val id: String,
    val name: String,
    val packages: List<String> = emptyList(),
    val iconName: String = "backpack"
)

@Serializable
data class FocusConfig(
    val durationMinutes: Int = 25,
    val allowedPackages: List<String> = emptyList(),
    val blockedPackages: List<String> = emptyList(), // Apps selected individually to be blocked
    val appBags: List<AppBag> = listOf(
        AppBag("socials", "Social Media", emptyList(), "socials"),
        AppBag("study", "Study / Work", emptyList(), "study"),
        AppBag("entertainment", "Entertainment", emptyList(), "entertainment")
    ),
    val activeBlockedBags: List<String> = emptyList(), // Bag IDs that are blocked in focus
    val passwordHash: String = "",
    val qrCodeKey: String = "", // Emergency QR code key string for unlock
    val exitKeyType: FocusExitKeyType = FocusExitKeyType.NONE,
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
