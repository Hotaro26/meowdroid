package com.bintianqi.owndroid.feature.time_blocker

import androidx.compose.runtime.Composable
import com.bintianqi.owndroid.ui.navigation.Destination

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
