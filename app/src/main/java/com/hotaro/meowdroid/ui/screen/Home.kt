package com.hotaro.meowdroid.ui.screen

import android.os.Build.VERSION
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hotaro.meowdroid.R
import com.hotaro.meowdroid.feature.focus.FocusScreen
import com.hotaro.meowdroid.feature.focus.FocusState
import com.hotaro.meowdroid.feature.focus.FocusViewModel
import com.hotaro.meowdroid.feature.time_blocker.TimeBlockerScreen
import com.hotaro.meowdroid.feature.time_blocker.TimeBlockerViewModel
import com.hotaro.meowdroid.ui.component.FloatingNavItem
import com.hotaro.meowdroid.ui.component.TomatoFloatingNavBar
import com.hotaro.meowdroid.ui.navigation.Destination
import com.hotaro.meowdroid.utils.BottomPadding
import com.hotaro.meowdroid.utils.PrivilegeStatus
import com.hotaro.meowdroid.utils.adaptiveInsets
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    privilegeState: StateFlow<PrivilegeStatus>,
    focusVm: FocusViewModel,
    blockerVm: TimeBlockerViewModel,
    onNavigate: (Destination) -> Unit
) {
    val privilege by privilegeState.collectAsState()
    val session by focusVm.sessionState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val isFocusActive = session.state == FocusState.ACTIVE

    val navItems = listOf(
        FloatingNavItem(
            label = "Focus",
            selectedIcon = painterResource(R.drawable.timer_filled),
            unselectedIcon = painterResource(R.drawable.timer_outlined)
        ),
        FloatingNavItem(
            label = "Blocker",
            selectedIcon = painterResource(R.drawable.monitoring_filled),
            unselectedIcon = painterResource(R.drawable.monitoring)
        ),
        FloatingNavItem(
            label = "Tools",
            selectedIcon = painterResource(R.drawable.shield_fill0),
            unselectedIcon = painterResource(R.drawable.shield_fill0)
        ),
        FloatingNavItem(
            label = "Settings",
            selectedIcon = painterResource(R.drawable.settings_filled),
            unselectedIcon = painterResource(R.drawable.settings)
        )
    )

    Scaffold(
        topBar = {
            if (!isFocusActive) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !isFocusActive,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                TomatoFloatingNavBar(
                    items = navItems,
                    selectedIndex = selectedTab,
                    onItemSelected = { selectedTab = it }
                )
            }
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        when (selectedTab) {
            0 -> {
                FocusScreen(
                    vm = focusVm,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            1 -> {
                TimeBlockerScreen(
                    vm = blockerVm,
                    navigate = onNavigate,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            2 -> {
                ToolsTab(
                    privilege = privilege,
                    modifier = Modifier.padding(paddingValues),
                    onNavigate = onNavigate
                )
            }
            3 -> {
                SettingsTab(
                    privilege = privilege,
                    modifier = Modifier.padding(paddingValues),
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun ToolsTab(
    privilege: PrivilegeStatus,
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.device_tools),
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate(Destination.Suspend) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.pause),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.suspend), style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text(stringResource(R.string.choose_working_mode), style = typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate(Destination.Hide) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.visibility_off_fill0),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.hide), style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("Hide selected apps from launcher", style = typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate(Destination.BlockUninstall) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.lock_fill0),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.block_uninstall), style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("Prevent critical apps from being uninstalled", style = typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (VERSION.SDK_INT >= 24) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(Destination.UserRestriction) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.security_fill0),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.user_restriction), style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("Manage user restrictions and hardware locks", style = typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsTab(
    privilege: PrivilegeStatus,
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        // Device Owner Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate(Destination.WorkingModes(true)) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.security_fill0),
                    contentDescription = null,
                    tint = if (privilege.device) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Device Owner", style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text(
                        text = if (privilege.device) "Active (Device Owner)" else if (privilege.profile) "Active (Profile Owner)" else "Unmanaged / Inactive",
                        style = typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Extra Settings Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate(Destination.Settings) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.settings_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Extra Settings", style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("Theme, security, logs and system settings", style = typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // About Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate(Destination.About) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("About MeowDroid", style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("Digital mindful device controller", style = typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}
