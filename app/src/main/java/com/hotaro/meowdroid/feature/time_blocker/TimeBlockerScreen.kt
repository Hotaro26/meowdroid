package com.hotaro.meowdroid.feature.time_blocker

import androidx.compose.runtime.Composable
import com.hotaro.meowdroid.ui.navigation.Destination

@Composable
fun TimeBlockerScreen(
    vm: TimeBlockerViewModel,
    onNavigate: (Destination) -> Unit,
    onNavigateUp: () -> Unit
) {
    TimeBlockerCalendarScreen(
        vm = vm,
        onNavigateUp = onNavigateUp
    )
}
