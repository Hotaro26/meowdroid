package com.hotaro.meowdroid.feature.focus

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hotaro.meowdroid.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Minimalist Focus Screen inspired by Tomato Pomodoro Timer.
 * - Circular progress ring gauge
 * - Tappable countdown clock that opens a customization bottom drawer
 * - Simple 2-button control bar (Play/Pause pill + Skip button)
 * - Zero emojis, pure Material 3 Expressive vector icons
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FocusScreen(
    vm: FocusViewModel,
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(false) }

    // Durations in seconds (default 25 minutes)
    var totalTimeSeconds by remember { mutableLongStateOf(25 * 60L) }
    var remainingTimeSeconds by remember { mutableLongStateOf(25 * 60L) }

    // Bottom drawer state
    var showCustomizeSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Realtime countdown loop
    LaunchedEffect(isRunning, remainingTimeSeconds) {
        if (isRunning && remainingTimeSeconds > 0) {
            delay(1000L)
            remainingTimeSeconds -= 1
        } else if (isRunning && remainingTimeSeconds <= 0L) {
            isRunning = false
        }
    }

    val progressFraction = if (totalTimeSeconds > 0) {
        (totalTimeSeconds - remainingTimeSeconds).toFloat() / totalTimeSeconds.toFloat()
    } else 0f

    val primaryColor = colorScheme.primary
    val onPrimaryColor = colorScheme.onPrimary
    val containerColor = colorScheme.primaryContainer
    val onContainerColor = colorScheme.onPrimaryContainer

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
                text = "Focus",
                style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = primaryColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Circular Timer Gauge (Tomato signature dial - Clickable to open drawer)
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
                    color = colorScheme.surfaceContainerHighest,
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

                // Centered Clickable Clock Display
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showCustomizeSheet = true
                        }
                        .padding(32.dp)
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
                }
            }
        }

        // Space
        item {
            Spacer(Modifier.height(36.dp))
        }

        // 2-Button Control Bar (Play/Pause Capsule + Skip Button)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                        .width(150.dp)
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

                // Skip Next Button (advances or finishes session)
                FilledTonalIconButton(
                    onClick = {
                        isRunning = false
                        remainingTimeSeconds = 0L
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colorScheme.surfaceContainerHighest,
                        contentColor = colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next_large),
                        contentDescription = "Skip",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }

    // Bottom Drawer for Customizing Countdown
    if (showCustomizeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomizeSheet = false },
            sheetState = sheetState,
            containerColor = colorScheme.surfaceContainerLow,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            var inputMinutes by remember {
                mutableStateOf((totalTimeSeconds / 60).toString())
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Customise Countdown",
                    style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Set how many minutes you want to focus",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Quick Preset Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20, 25, 30, 45, 60, 90).forEach { mins ->
                        val isSelected = inputMinutes == mins.toString()
                        Surface(
                            onClick = { inputMinutes = mins.toString() },
                            shape = CircleShape,
                            color = if (isSelected) colorScheme.primary else colorScheme.surfaceContainerHigh,
                            tonalElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Text(
                                text = "${mins}m",
                                style = typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Custom Minute Input Field
                OutlinedTextField(
                    value = inputMinutes,
                    onValueChange = { newText ->
                        val filtered = newText.filter { it.isDigit() }.take(3)
                        inputMinutes = filtered
                    },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                // Apply Button
                Button(
                    onClick = {
                        val parsed = inputMinutes.toLongOrNull() ?: 25L
                        val validMinutes = parsed.coerceIn(1L, 300L)
                        totalTimeSeconds = validMinutes * 60L
                        remainingTimeSeconds = validMinutes * 60L
                        isRunning = false

                        scope.launch {
                            sheetState.hide()
                            showCustomizeSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(
                        text = "Set Countdown",
                        style = typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
