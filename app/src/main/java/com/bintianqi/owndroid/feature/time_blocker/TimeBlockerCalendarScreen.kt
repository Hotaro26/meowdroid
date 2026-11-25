package com.bintianqi.owndroid.feature.time_blocker

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.formatMinutes
import com.bintianqi.owndroid.utils.formatTimeRange
import com.bintianqi.owndroid.utils.getAppInfo
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

private const val HOUR_HEIGHT_DP = 64

data class CalendarBlockItem(
    val ruleId: Int,
    val rule: BlockRule,
    val windowIndex: Int,
    val window: TimeWindow,
    val appInfo: AppInfo
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimeBlockerCalendarScreen(
    vm: TimeBlockerViewModel,
    modifier: Modifier = Modifier,
    onNavigateUp: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val rules by vm.rulesState.collectAsState()
    val isRunning by vm.serviceRunning.collectAsState()

    val todayDate = remember { LocalDate.now() }
    val todayDayOfWeek = todayDate.dayOfWeek.value // 1=Mon .. 7=Sun
    var selectedDayOfWeek by rememberSaveable { mutableIntStateOf(todayDayOfWeek) }

    // Bottom Drawer Sheet State
    var showDrawer by remember { mutableStateOf(false) }
    var editingRuleId by remember { mutableIntStateOf(-1) }
    var editingWindowIndex by remember { mutableIntStateOf(-1) }

    var selectedPackageName by remember { mutableStateOf("") }
    var drawerStartMinutes by remember { mutableIntStateOf(540) } // 9:00 AM
    var drawerEndMinutes by remember { mutableIntStateOf(660) }   // 11:00 AM
    var drawerDaysOfWeek by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) }

    // App Picker Dialog
    var showAppPicker by remember { mutableStateOf(false) }
    var installedApps by remember { mutableStateOf(emptyList<AppInfo>()) }

    // Time Pickers
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Load installed apps for selection
    LaunchedEffect(Unit) {
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        installedApps = packages.filter { app ->
            pm.getLaunchIntentForPackage(app.packageName) != null &&
                    app.packageName != context.packageName
        }.map { getAppInfo(pm, it) }.sortedBy { it.label.lowercase() }
    }

    // Scroll state: Auto scroll near current time on launch
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
        val targetHour = (nowMin / 60 - 1).coerceAtLeast(0)
        scrollState.scrollTo(targetHour * HOUR_HEIGHT_DP * 2)
    }

    // Filter blocks for selected day of week
    val blocksForSelectedDay = remember(rules, selectedDayOfWeek, installedApps) {
        val list = mutableListOf<CalendarBlockItem>()
        for (rule in rules) {
            val app = getAppInfo(pm, rule.packageName)
            rule.blockedWindows.forEachIndexed { index, window ->
                if (window.daysOfWeek.contains(selectedDayOfWeek)) {
                    list.add(CalendarBlockItem(rule.id, rule, index, window, app))
                }
            }
        }
        list
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onNavigateUp != null) {
                                IconButton(onClick = onNavigateUp) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                }
                            }
                            Icon(
                                painter = painterResource(R.drawable.calendar_month_fill0),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.time_blocker),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Service Status Pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(if (isRunning) R.string.enabled else R.string.disabled),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(6.dp))
                                Switch(
                                    checked = isRunning,
                                    onCheckedChange = { if (it) vm.startService() else vm.stopService() },
                                    modifier = Modifier.size(height = 24.dp, width = 36.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Google Calendar Weekday Selector Strip
                    val startOfWeek = todayDate.minusDays((todayDayOfWeek - 1).toLong())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 1..7) {
                            val dayDate = startOfWeek.plusDays((i - 1).toLong())
                            val dayName = dayDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3)
                            val isSelected = selectedDayOfWeek == i
                            val isToday = todayDayOfWeek == i

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { selectedDayOfWeek = i }
                                    .padding(horizontal = 2.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = dayDate.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                        )
                                    } else {
                                        Spacer(Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.block_time), fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = {
                    editingRuleId = -1
                    editingWindowIndex = -1
                    selectedPackageName = installedApps.firstOrNull()?.name ?: ""
                    drawerStartMinutes = 540
                    drawerEndMinutes = 660
                    drawerDaysOfWeek = setOf(selectedDayOfWeek)
                    showDrawer = true
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        // 24-Hour Calendar Canvas Grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            val canvasHeight = (24 * HOUR_HEIGHT_DP).dp

            // Hour Grid Lines & Labels
            Column(modifier = Modifier.fillMaxWidth()) {
                for (hour in 0..23) {
                    val formattedHour = when {
                        hour == 0 -> "12 AM"
                        hour < 12 -> "$hour AM"
                        hour == 12 -> "12 PM"
                        else -> "${hour - 12} PM"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(HOUR_HEIGHT_DP.dp)
                            .clickable {
                                // Tapping empty slot creates a block for that hour
                                editingRuleId = -1
                                editingWindowIndex = -1
                                selectedPackageName = installedApps.firstOrNull()?.name ?: ""
                                drawerStartMinutes = hour * 60
                                drawerEndMinutes = ((hour + 1) * 60).coerceAtMost(1439)
                                drawerDaysOfWeek = setOf(selectedDayOfWeek)
                                showDrawer = true
                            },
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = formattedHour,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .width(54.dp)
                                .padding(start = 12.dp, top = 2.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Current Time Indicator Line (shown only on today's view)
            if (selectedDayOfWeek == todayDayOfWeek) {
                val currentMinutes = LocalTime.now().let { it.hour * 60 + it.minute }
                val currentLineY = ((currentMinutes / 60f) * HOUR_HEIGHT_DP).dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = currentLineY)
                        .padding(start = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.error,
                        thickness = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Render Time Block Cards on Timeline
            blocksForSelectedDay.forEach { item ->
                val start = item.window.startMinutes
                val end = if (item.window.endMinutes > start) item.window.endMinutes else 1440
                val blockTop = ((start / 60f) * HOUR_HEIGHT_DP).dp
                val blockHeight = (((end - start) / 60f) * HOUR_HEIGHT_DP).dp.coerceAtLeast(36.dp)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 58.dp, end = 16.dp)
                        .offset(y = blockTop)
                        .height(blockHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            // Edit existing block
                            editingRuleId = item.ruleId
                            editingWindowIndex = item.windowIndex
                            selectedPackageName = item.rule.packageName
                            drawerStartMinutes = item.window.startMinutes
                            drawerEndMinutes = item.window.endMinutes
                            drawerDaysOfWeek = item.window.daysOfWeek
                            showDrawer = true
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored accent stripe
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.width(10.dp))
                        Image(
                            painter = rememberDrawablePainter(item.appInfo.icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.appInfo.label,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatTimeRange(item.window.startMinutes, item.window.endMinutes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    // Google Calendar-Style Bottom Drawer (ModalBottomSheet) to customize block
    if (showDrawer) {
        val selectedApp = installedApps.find { it.name == selectedPackageName }
            ?: installedApps.firstOrNull()

        ModalBottomSheet(
            onDismissRequest = { showDrawer = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // Drawer Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingRuleId >= 0) stringResource(R.string.edit_time_block) else stringResource(R.string.new_time_block),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (editingRuleId >= 0) {
                        IconButton(onClick = {
                            val rule = rules.find { it.id == editingRuleId }
                            if (rule != null) {
                                val remainingWindows = rule.blockedWindows.toMutableList()
                                if (editingWindowIndex in remainingWindows.indices) {
                                    remainingWindows.removeAt(editingWindowIndex)
                                }
                                if (remainingWindows.isEmpty() && rule.dailyLimitMinutes == 0) {
                                    vm.deleteRule(rule.id)
                                } else {
                                    vm.updateRule(rule.copy(blockedWindows = remainingWindows))
                                }
                            }
                            showDrawer = false
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Target App Selector Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showAppPicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedApp != null) {
                            Image(
                                painter = rememberDrawablePainter(selectedApp.icon),
                                contentDescription = null,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = selectedApp.label,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = selectedApp.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.apps_fill0),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = stringResource(R.string.select_target_app),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        TextButton(onClick = { showAppPicker = true }) {
                            Text("Change")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Time Pickers Row (Start Time -> End Time)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Time Box
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showStartTimePicker = true }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.start_time),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatMinutes(drawerStartMinutes),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // End Time Box
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showEndTimePicker = true }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.end_time),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatMinutes(drawerEndMinutes),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Repeating Days of Week Selector
                Text(
                    text = stringResource(R.string.repeat_days),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(8.dp))

                val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayIdx in 1..7) {
                        val isDaySelected = drawerDaysOfWeek.contains(dayIdx)
                        Surface(
                            shape = CircleShape,
                            color = if (isDaySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable {
                                    drawerDaysOfWeek = if (isDaySelected) {
                                        if (drawerDaysOfWeek.size > 1) drawerDaysOfWeek - dayIdx else drawerDaysOfWeek
                                    } else {
                                        drawerDaysOfWeek + dayIdx
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = dayLabels[dayIdx - 1],
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDaySelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Preset Day Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { drawerDaysOfWeek = (1..7).toSet() },
                        label = { Text(stringResource(R.string.everyday)) }
                    )
                    AssistChip(
                        onClick = { drawerDaysOfWeek = (1..5).toSet() },
                        label = { Text(stringResource(R.string.weekdays)) }
                    )
                    AssistChip(
                        onClick = { drawerDaysOfWeek = setOf(6, 7) },
                        label = { Text(stringResource(R.string.weekends)) }
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Confirm / Save Button
                Button(
                    onClick = {
                        val targetPkg = selectedApp?.name ?: selectedPackageName
                        if (targetPkg.isNotEmpty()) {
                            val newWindow = TimeWindow(
                                startMinutes = drawerStartMinutes,
                                endMinutes = drawerEndMinutes,
                                daysOfWeek = drawerDaysOfWeek
                            )

                            if (editingRuleId >= 0) {
                                // Update existing rule
                                val rule = rules.find { it.id == editingRuleId }
                                if (rule != null) {
                                    val updatedWindows = rule.blockedWindows.toMutableList()
                                    if (editingWindowIndex in updatedWindows.indices) {
                                        updatedWindows[editingWindowIndex] = newWindow
                                    } else {
                                        updatedWindows.add(newWindow)
                                    }
                                    vm.updateRule(rule.copy(packageName = targetPkg, blockedWindows = updatedWindows))
                                }
                            } else {
                                // Add new rule or append window to existing package rule
                                val existing = rules.find { it.packageName == targetPkg }
                                if (existing != null) {
                                    vm.updateRule(existing.copy(blockedWindows = existing.blockedWindows + newWindow))
                                } else {
                                    vm.addRule(
                                        BlockRule(
                                            packageName = targetPkg,
                                            blockedWindows = listOf(newWindow),
                                            enabled = true
                                        )
                                    )
                                }
                            }
                        }
                        showDrawer = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = stringResource(R.string.save_block),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // App Picker Dialog (Searchable)
    if (showAppPicker) {
        var appSearchQuery by remember { mutableStateOf("") }
        val filtered = installedApps.filter {
            it.label.contains(appSearchQuery, ignoreCase = true) || it.name.contains(appSearchQuery, ignoreCase = true)
        }

        ModalBottomSheet(
            onDismissRequest = { showAppPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_target_app),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = appSearchQuery,
                    onValueChange = { appSearchQuery = it },
                    placeholder = { Text("Search apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(filtered, key = { it.name }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedPackageName = app.name
                                    showAppPicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberDrawablePainter(app.icon),
                                contentDescription = null,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(app.label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                Text(app.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (selectedPackageName == app.name) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = drawerStartMinutes / 60,
            initialMinute = drawerStartMinutes % 60,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text(stringResource(R.string.start_time)) },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = pickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    drawerStartMinutes = pickerState.hour * 60 + pickerState.minute
                    showStartTimePicker = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = drawerEndMinutes / 60,
            initialMinute = drawerEndMinutes % 60,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text(stringResource(R.string.end_time)) },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = pickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    drawerEndMinutes = pickerState.hour * 60 + pickerState.minute
                    showEndTimePicker = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
