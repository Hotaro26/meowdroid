package com.bintianqi.owndroid.ui.screen

import android.os.Build.VERSION
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.applications.AppChooserMode
import com.bintianqi.owndroid.feature.focus.FocusScreen
import com.bintianqi.owndroid.feature.focus.FocusState
import com.bintianqi.owndroid.feature.focus.FocusViewModel
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.adaptiveInsets
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    privilegeState: StateFlow<PrivilegeStatus>,
    focusVm: FocusViewModel,
    onNavigate: (Destination) -> Unit
) {
    val privilege by privilegeState.collectAsState()
    val session by focusVm.sessionState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val isFocusActive = session.state == FocusState.ACTIVE

    Scaffold(
        topBar = {
            if (!isFocusActive) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.app_name),
                                style = typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (privilege.device)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = if (privilege.device) "Device Owner" else if (privilege.profile) "Profile Owner" else "Unmanaged",
                                    style = typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton({ onNavigate(Destination.WorkingModes(true)) }) {
                            Icon(painterResource(R.drawable.security_fill0), contentDescription = "Working Modes")
                        }
                        IconButton({ onNavigate(Destination.Settings) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isFocusActive) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(painterResource(R.drawable.lock_clock_fill0), contentDescription = null) },
                        label = { Text(stringResource(R.string.focus_mode)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(painterResource(R.drawable.timer_fill0), contentDescription = null) },
                        label = { Text(stringResource(R.string.time_blocker)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(painterResource(R.drawable.shield_fill0), contentDescription = null) },
                        label = { Text(stringResource(R.string.device_tools)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.settings)) }
                    )
                }
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
                BlockerTab(
                    modifier = Modifier.padding(paddingValues),
                    onNavigate = onNavigate
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
                    modifier = Modifier.padding(paddingValues),
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun BlockerTab(
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.timer_fill0),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = stringResource(R.string.time_blocker),
                        style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.time_blocker_dashboard_desc),
                    style = typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        HomePageItem(R.string.time_blocker, R.drawable.timer_fill0) {
            onNavigate(Destination.TimeBlocker)
        }
        HomePageItem(R.string.suspend, R.drawable.volume_off_fill0) {
            onNavigate(Destination.Suspend)
        }
        HomePageItem(R.string.hide, R.drawable.visibility_off_fill0) {
            onNavigate(Destination.Hide)
        }
        HomePageItem(R.string.block_uninstall, R.drawable.delete_forever_fill0) {
            onNavigate(Destination.BlockUninstall)
        }
        if (VERSION.SDK_INT >= 24) {
            HomePageItem(R.string.user_restriction, R.drawable.person_off) {
                onNavigate(Destination.UserRestriction)
            }
        }

        Spacer(Modifier.height(BottomPadding))
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
    ) {
        if (privilege.device || privilege.profile) {
            HomePageItem(R.string.system, R.drawable.android_fill0) {
                onNavigate(Destination.System)
            }
            HomePageItem(R.string.network, R.drawable.wifi_fill0) { onNavigate(Destination.Network) }
        }
        if (privilege.work) {
            HomePageItem(R.string.work_profile, R.drawable.work_fill0) {
                onNavigate(Destination.WorkProfile)
            }
        }
        if (privilege.device || privilege.profile) {
            HomePageItem(R.string.applications, R.drawable.apps_fill0) {
                onNavigate(Destination.ApplicationsList(AppChooserMode.ListView))
            }
            HomePageItem(R.string.applications_state, R.drawable.tune_fill0) {
                onNavigate(Destination.ApplicationFeatures)
            }
            HomePageItem(R.string.users, R.drawable.manage_accounts_fill0) {
                onNavigate(Destination.Users)
            }
            HomePageItem(
                R.string.password_and_keyguard, R.drawable.password_fill0
            ) { onNavigate(Destination.Password) }
        }
        Spacer(Modifier.height(BottomPadding))
    }
}

@Composable
private fun SettingsTab(
    modifier: Modifier = Modifier,
    onNavigate: (Destination) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HomePageItem(R.string.choose_working_mode, R.drawable.security_fill0) {
            onNavigate(Destination.WorkingModes(true))
        }
        HomePageItem(R.string.app_lock, R.drawable.lock_fill0) {
            onNavigate(Destination.AppLockSettings)
        }
        HomePageItem(R.string.appearance, R.drawable.format_paint_fill0) {
            onNavigate(Destination.AppearanceSettings)
        }
        HomePageItem(R.string.settings, R.drawable.settings_fill0) {
            onNavigate(Destination.Settings)
        }
        HomePageItem(R.string.about, R.drawable.info_fill0) {
            onNavigate(Destination.About)
        }
        Spacer(Modifier.height(BottomPadding))
    }
}

@Composable
fun HomePageItem(name: Int, icon: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.padding(start = 24.dp))
        Icon(painterResource(icon), contentDescription = null, Modifier.size(24.dp))
        Spacer(Modifier.padding(start = 16.dp))
        Text(stringResource(name), style = typography.titleMedium)
    }
}
