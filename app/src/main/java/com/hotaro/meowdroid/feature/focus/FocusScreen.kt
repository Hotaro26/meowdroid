package com.hotaro.meowdroid.feature.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotaro.meowdroid.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class TomatoTimerMode {
    FOCUS, SHORT_BREAK, LONG_BREAK
}

/**
 * Authentic Tomato-style Focus Timer Window.
 * Features:
 * - Minimalist dynamic circular progress ring with seamless color transitions
 * - Monospace large countdown clock with session indicator
 * - Tomato-style 3-button control group (Play/Pause pill, Restart button, Skip button)
 * - Mode selector chips (Focus 25m, Short Break 5m, Long Break 15m)
 * - Built on Material 3 Expressive styling with zero emojis
 */
@Composable
fun FocusScreen(
    vm: FocusViewModel,
    modifier: Modifier = Modifier
) {
    var timerMode by remember { mutableStateOf(TomatoTimerMode.FOCUS) }
    var isRunning by remember { mutableStateOf(false) }

    // Durations in seconds
    val focusDuration = 25 * 60L
    val shortBreakDuration = 5 * 60L
    val longBreakDuration = 15 * 60L

    var totalTimeSeconds by remember { mutableLongStateOf(focusDuration) }
    var remainingTimeSeconds by remember { mutableLongStateOf(focusDuration) }
    var currentSessionCount by remember { mutableIntStateOf(1) }
    val totalSessions = 4

    val haptic = LocalHapticFeedback.current

    // Timer countdown loop
    LaunchedEffect(isRunning, remainingTimeSeconds) {
        if (isRunning && remainingTimeSeconds > 0) {
            delay(1000L)
            remainingTimeSeconds -= 1
        } else if (isRunning && remainingTimeSeconds <= 0L) {
            isRunning = false
            // Auto transition to break or next session
            if (timerMode == TomatoTimerMode.FOCUS) {
                if (currentSessionCount >= totalSessions) {
                    timerMode = TomatoTimerMode.LONG_BREAK
                    totalTimeSeconds = longBreakDuration
                    remainingTimeSeconds = longBreakDuration
                } else {
                    timerMode = TomatoTimerMode.SHORT_BREAK
                    totalTimeSeconds = shortBreakDuration
                    remainingTimeSeconds = shortBreakDuration
                }
            } else {
                if (timerMode == TomatoTimerMode.LONG_BREAK) {
                    currentSessionCount = 1
                } else {
                    currentSessionCount += 1
                }
                timerMode = TomatoTimerMode.FOCUS
                totalTimeSeconds = focusDuration
                remainingTimeSeconds = focusDuration
            }
        }
    }

    val progressFraction = if (totalTimeSeconds > 0) {
        (totalTimeSeconds - remainingTimeSeconds).toFloat() / totalTimeSeconds.toFloat()
    } else 0f

    val modeFraction by animateFloatAsState(
        targetValue = if (timerMode == TomatoTimerMode.FOCUS) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "modeFraction"
    )

    val primaryColor = lerp(colorScheme.tertiary, colorScheme.primary, modeFraction)
    val onPrimaryColor = lerp(colorScheme.onTertiary, colorScheme.onPrimary, modeFraction)
    val containerColor = lerp(colorScheme.tertiaryContainer, colorScheme.secondaryContainer, modeFraction)
    val onContainerColor = lerp(colorScheme.onTertiaryContainer, colorScheme.onSecondaryContainer, modeFraction)

    val minutes = remainingTimeSeconds / 60
    val seconds = remainingTimeSeconds % 60
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mode Title Header
        item {
            Text(
                text = when (timerMode) {
                    TomatoTimerMode.FOCUS -> "Focus"
                    TomatoTimerMode.SHORT_BREAK -> "Short Break"
                    TomatoTimerMode.LONG_BREAK -> "Long Break"
                },
                style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = primaryColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        // Circular Timer Gauge (Tomato signature dial)
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .aspectRatio(1f)
                    .padding(8.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    color = containerColor,
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = Color.Transparent
                )

                // Animated Progress Ring
                CircularProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    color = primaryColor,
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = Color.Transparent
                )

                // Centered Clock Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = timeStr,
                        style = TextStyle(
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-2).sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Surface(
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "$currentSessionCount of $totalSessions",
                            style = typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Space
        item {
            Spacer(Modifier.height(28.dp))
        }

        // Tomato 3-Button Control Bar
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Capsule Toggle
                Surface(
                    onClick = {
                        isRunning = !isRunning
                    },
                    shape = RoundedCornerShape(28.dp),
                    color = primaryColor,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .height(72.dp)
                        .width(136.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isRunning) R.drawable.pause_large else R.drawable.play_large
                            ),
                            contentDescription = if (isRunning) "Pause" else "Play",
                            tint = onPrimaryColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Reset / Restart Button
                FilledTonalIconButton(
                    onClick = {
                        isRunning = false
                        remainingTimeSeconds = totalTimeSeconds
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = containerColor,
                        contentColor = onContainerColor
                    ),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.restart_large),
                        contentDescription = "Restart",
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Skip Next Button
                FilledTonalIconButton(
                    onClick = {
                        isRunning = false
                        if (timerMode == TomatoTimerMode.FOCUS) {
                            timerMode = TomatoTimerMode.SHORT_BREAK
                            totalTimeSeconds = shortBreakDuration
                            remainingTimeSeconds = shortBreakDuration
                        } else {
                            currentSessionCount = if (currentSessionCount < totalSessions) currentSessionCount + 1 else 1
                            timerMode = TomatoTimerMode.FOCUS
                            totalTimeSeconds = focusDuration
                            remainingTimeSeconds = focusDuration
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = containerColor,
                        contentColor = onContainerColor
                    ),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next_large),
                        contentDescription = "Skip to Next",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        // Space
        item {
            Spacer(Modifier.height(32.dp))
        }

        // Quick Mode Switcher Pill Row
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeChip(
                    label = "Focus 25m",
                    selected = timerMode == TomatoTimerMode.FOCUS,
                    onClick = {
                        isRunning = false
                        timerMode = TomatoTimerMode.FOCUS
                        totalTimeSeconds = focusDuration
                        remainingTimeSeconds = focusDuration
                    }
                )
                ModeChip(
                    label = "Short 5m",
                    selected = timerMode == TomatoTimerMode.SHORT_BREAK,
                    onClick = {
                        isRunning = false
                        timerMode = TomatoTimerMode.SHORT_BREAK
                        totalTimeSeconds = shortBreakDuration
                        remainingTimeSeconds = shortBreakDuration
                    }
                )
                ModeChip(
                    label = "Long 15m",
                    selected = timerMode == TomatoTimerMode.LONG_BREAK,
                    onClick = {
                        isRunning = false
                        timerMode = TomatoTimerMode.LONG_BREAK
                        totalTimeSeconds = longBreakDuration
                        remainingTimeSeconds = longBreakDuration
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            style = typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
