package com.hotaro.meowdroid.feature.focus

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.hotaro.meowdroid.R
import com.hotaro.meowdroid.feature.settings.QrUtils
import com.hotaro.meowdroid.utils.AppInfo

/**
 * Zen Settings Screen:
 * 1. Which apps to block in focus (multi-select app list)
 * 2. Key section:
 *    - None
 *    - Password (custom PIN/Password with confirmation)
 *    - QR Code (emergency printable/scannable QR code key)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenSettingsScreen(
    vm: FocusViewModel,
    onNavigateBack: () -> Unit
) {
    val config by vm.configState.collectAsState()
    val installedApps by vm.installedApps.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Apps to block, 1: Key Section

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Zen Settings",
                        style = typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Apps to Block (${config.blockedPackages.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.block_fill0),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Exit Key",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.key_fill0),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> AppsToBlockTab(
                    installedApps = installedApps,
                    blockedPackages = config.blockedPackages,
                    onToggleBlock = { vm.toggleBlockedPackage(it) }
                )
                1 -> KeySectionTab(
                    config = config,
                    vm = vm
                )
            }
        }
    }
}

@Composable
private fun AppsToBlockTab(
    installedApps: List<AppInfo>,
    blockedPackages: List<String>,
    onToggleBlock: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed apps...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Apps marked here will be suspended/blocked while Focus is running.",
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.name }) { app ->
                val isBlocked = blockedPackages.contains(app.name)
                ElevatedCard(
                    onClick = { onToggleBlock(app.name) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isBlocked) colorScheme.errorContainer else colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberDrawablePainter(app.icon),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isBlocked) colorScheme.onErrorContainer else colorScheme.onSurface
                            )
                            Text(
                                text = app.name,
                                style = typography.bodySmall,
                                color = if (isBlocked) colorScheme.onErrorContainer.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isBlocked) colorScheme.error else colorScheme.surfaceContainerHigh
                        ) {
                            Icon(
                                imageVector = if (isBlocked) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isBlocked) colorScheme.onError else colorScheme.outline,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun KeySectionTab(
    config: FocusConfig,
    vm: FocusViewModel
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var qrBitmap by remember(config.qrCodeKey) {
        mutableStateOf<Bitmap?>(null)
    }

    // Generate QR bitmap if QR is chosen and key exists
    androidx.compose.runtime.LaunchedEffect(config.qrCodeKey) {
        if (config.qrCodeKey.isNotBlank()) {
            try {
                qrBitmap = QrUtils.encodeToBitmap(config.qrCodeKey.toByteArray(Charsets.ISO_8859_1), 400)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Emergency Exit Key",
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.primary
        )

        Text(
            text = "Choose how to unlock or stop your focus session early if necessary. This prevents impulsive distraction.",
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )

        // Option 1: None
        ElevatedCard(
            onClick = { vm.setExitKeyType(FocusExitKeyType.NONE) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (config.exitKeyType == FocusExitKeyType.NONE) colorScheme.primaryContainer else colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = config.exitKeyType == FocusExitKeyType.NONE,
                    onClick = { vm.setExitKeyType(FocusExitKeyType.NONE) }
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("No Key (Normal)", style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("Can end session anytime without password", style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
                }
            }
        }

        // Option 2: Password / PIN
        ElevatedCard(
            onClick = {
                vm.setExitKeyType(FocusExitKeyType.PASSWORD)
                showPasswordDialog = true
            },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (config.exitKeyType == FocusExitKeyType.PASSWORD) colorScheme.primaryContainer else colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = config.exitKeyType == FocusExitKeyType.PASSWORD,
                        onClick = {
                            vm.setExitKeyType(FocusExitKeyType.PASSWORD)
                            showPasswordDialog = true
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Password / PIN", style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            text = if (config.passwordHash.isNotBlank()) "Password set" else "Tap to configure password",
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    if (config.exitKeyType == FocusExitKeyType.PASSWORD) {
                        Button(
                            onClick = { showPasswordDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Change")
                        }
                    }
                }
            }
        }

        // Option 3: QR Code
        ElevatedCard(
            onClick = {
                vm.setExitKeyType(FocusExitKeyType.QR_CODE)
                if (config.qrCodeKey.isBlank()) {
                    vm.generateNewQrCodeKey()
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (config.exitKeyType == FocusExitKeyType.QR_CODE) colorScheme.primaryContainer else colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = config.exitKeyType == FocusExitKeyType.QR_CODE,
                        onClick = {
                            vm.setExitKeyType(FocusExitKeyType.QR_CODE)
                            if (config.qrCodeKey.isBlank()) {
                                vm.generateNewQrCodeKey()
                            }
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Physical QR Code Key", style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("Print or store this QR code in another room to unlock early", style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }
                }

                if (config.exitKeyType == FocusExitKeyType.QR_CODE) {
                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorScheme.surface, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        qrBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Emergency Unlock QR Code",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Key Code: ${config.qrCodeKey}",
                            style = typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = colorScheme.onSurface
                        )

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = { vm.generateNewQrCodeKey() }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Generate New QR Key")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Set Exit Password") },
            text = {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Enter PIN or Password") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    vm.setPassword(passwordInput)
                    passwordInput = ""
                    showPasswordDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    passwordInput = ""
                    showPasswordDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
