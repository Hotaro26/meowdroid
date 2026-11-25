package com.hotaro.meowdroid.feature.focus

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.hotaro.meowdroid.R
import com.hotaro.meowdroid.feature.settings.QrUtils
import com.hotaro.meowdroid.utils.AppInfo
import kotlinx.coroutines.launch
import java.util.UUID

enum class ZenDrawerType {
    NONE, APPS, KEY, BAGS, BAG_PACKER
}

/**
 * Material 3 Expressive Zen Settings Screen:
 * Clean, card-based section layout featuring:
 * 1. Apps Section -> Opens bottom drawer with searchable installed app blocker
 * 2. Key Section -> Opens bottom drawer with None / Password / QR Key options
 * 3. Bags Section -> Opens bottom drawer with app bags (e.g. Socials, Study, Work)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenSettingsScreen(
    vm: FocusViewModel,
    onNavigateBack: () -> Unit
) {
    val config by vm.configState.collectAsState()
    val installedApps by vm.installedApps.collectAsState()

    var activeDrawer by remember { mutableStateOf(ZenDrawerType.NONE) }
    var selectedBagForPacking by remember { mutableStateOf<AppBag?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Expressive Card
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = colorScheme.primaryContainer,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.mindfulness_fill0),
                                contentDescription = null,
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Focus Guard",
                            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Configure blocks, lock keys, and packs to protect your deep work.",
                            style = typography.bodySmall,
                            color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = "CONFIGURATIONS",
                style = typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                color = colorScheme.primary,
                modifier = Modifier.padding(start = 6.dp, top = 8.dp)
            )

            // Section 1: Apps to Block
            ZenSectionCard(
                icon = R.drawable.block_fill0,
                title = "Apps to Block",
                subtitle = if (config.blockedPackages.isEmpty()) "No apps blocked individually" else "${config.blockedPackages.size} apps blocked",
                badgeText = "${config.blockedPackages.size}",
                onClick = { activeDrawer = ZenDrawerType.APPS }
            )

            // Section 2: Key Section (Password or QR Code)
            ZenSectionCard(
                icon = R.drawable.key_fill0,
                title = "Emergency Key",
                subtitle = when (config.exitKeyType) {
                    FocusExitKeyType.NONE -> "No key (Normal mode)"
                    FocusExitKeyType.PASSWORD -> "Password / PIN protection active"
                    FocusExitKeyType.QR_CODE -> "Physical QR code key active"
                },
                badgeText = when (config.exitKeyType) {
                    FocusExitKeyType.NONE -> "Off"
                    FocusExitKeyType.PASSWORD -> "PIN"
                    FocusExitKeyType.QR_CODE -> "QR"
                },
                onClick = { activeDrawer = ZenDrawerType.KEY }
            )

            // Section 3: Bags Section (Pack apps into categories like Socials, Study, etc.)
            ZenSectionCard(
                icon = R.drawable.backpack_fill0,
                title = "App Bags",
                subtitle = "${config.appBags.size} bags created (${config.activeBlockedBags.size} currently active in focus)",
                badgeText = "${config.activeBlockedBags.size} active",
                onClick = { activeDrawer = ZenDrawerType.BAGS }
            )

            Spacer(Modifier.height(80.dp))
        }
    }

    // Bottom Drawer for APPS
    if (activeDrawer == ZenDrawerType.APPS) {
        ModalBottomSheet(
            onDismissRequest = { activeDrawer = ZenDrawerType.NONE },
            sheetState = sheetState,
            containerColor = colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            AppsDrawerContent(
                installedApps = installedApps,
                blockedPackages = config.blockedPackages,
                onToggleBlock = { vm.toggleBlockedPackage(it) },
                onClose = {
                    scope.launch {
                        sheetState.hide()
                        activeDrawer = ZenDrawerType.NONE
                    }
                }
            )
        }
    }

    // Bottom Drawer for KEY
    if (activeDrawer == ZenDrawerType.KEY) {
        ModalBottomSheet(
            onDismissRequest = { activeDrawer = ZenDrawerType.NONE },
            sheetState = sheetState,
            containerColor = colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            KeyDrawerContent(
                config = config,
                vm = vm,
                onClose = {
                    scope.launch {
                        sheetState.hide()
                        activeDrawer = ZenDrawerType.NONE
                    }
                }
            )
        }
    }

    // Bottom Drawer for BAGS
    if (activeDrawer == ZenDrawerType.BAGS) {
        ModalBottomSheet(
            onDismissRequest = { activeDrawer = ZenDrawerType.NONE },
            sheetState = sheetState,
            containerColor = colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            BagsDrawerContent(
                config = config,
                vm = vm,
                onPackBag = { bag ->
                    selectedBagForPacking = bag
                    activeDrawer = ZenDrawerType.BAG_PACKER
                },
                onClose = {
                    scope.launch {
                        sheetState.hide()
                        activeDrawer = ZenDrawerType.NONE
                    }
                }
            )
        }
    }

    // Bottom Drawer for PACKING SPECIFIC BAG
    if (activeDrawer == ZenDrawerType.BAG_PACKER && selectedBagForPacking != null) {
        ModalBottomSheet(
            onDismissRequest = { activeDrawer = ZenDrawerType.BAGS },
            sheetState = sheetState,
            containerColor = colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            BagPackerDrawerContent(
                bag = config.appBags.find { it.id == selectedBagForPacking?.id } ?: selectedBagForPacking!!,
                installedApps = installedApps,
                onTogglePackage = { pkg ->
                    selectedBagForPacking?.id?.let { vm.togglePackageInBag(it, pkg) }
                },
                onDone = {
                    activeDrawer = ZenDrawerType.BAGS
                }
            )
        }
    }
}

@Composable
private fun ZenSectionCard(
    icon: Int,
    title: String,
    subtitle: String,
    badgeText: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = colorScheme.secondaryContainer
            ) {
                Text(
                    text = badgeText,
                    style = typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.outline
            )
        }
    }
}

// -------------------------------------------------------------
// 1. APPS DRAWER CONTENT
// -------------------------------------------------------------
@Composable
private fun AppsDrawerContent(
    installedApps: List<AppInfo>,
    blockedPackages: List<String>,
    onToggleBlock: (String) -> Unit,
    onClose: () -> Unit
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
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Apps to Block",
                    style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = "${blockedPackages.size} apps will be suspended during focus",
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search apps...") },
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

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.name }) { app ->
                val isBlocked = blockedPackages.contains(app.name)
                ElevatedCard(
                    onClick = { onToggleBlock(app.name) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isBlocked) colorScheme.errorContainer else colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberDrawablePainter(app.icon),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(42.dp)
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
                                color = if (isBlocked) colorScheme.onErrorContainer.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isBlocked) colorScheme.error else colorScheme.surfaceContainerHighest
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
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// 2. KEY DRAWER CONTENT
// -------------------------------------------------------------
@Composable
private fun KeyDrawerContent(
    config: FocusConfig,
    vm: FocusViewModel,
    onClose: () -> Unit
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var qrBitmap by remember(config.qrCodeKey) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(config.qrCodeKey) {
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
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Emergency Exit Key",
                    style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Choose a lock to prevent impulsively breaking focus",
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(Modifier.height(18.dp))

        // Option 1: None
        ElevatedCard(
            onClick = { vm.setExitKeyType(FocusExitKeyType.NONE) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (config.exitKeyType == FocusExitKeyType.NONE) colorScheme.primaryContainer else colorScheme.surfaceContainerHigh
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
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("No Key (Normal)", style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                    Text("Can end or pause focus anytime", style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Option 2: Password / PIN
        ElevatedCard(
            onClick = {
                vm.setExitKeyType(FocusExitKeyType.PASSWORD)
                showPasswordDialog = true
            },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (config.exitKeyType == FocusExitKeyType.PASSWORD) colorScheme.primaryContainer else colorScheme.surfaceContainerHigh
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
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Password / PIN", style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            text = if (config.passwordHash.isNotBlank()) "Password saved" else "Tap to set password",
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    if (config.exitKeyType == FocusExitKeyType.PASSWORD) {
                        Button(
                            onClick = { showPasswordDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Edit")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Option 3: Physical QR Code Key
        ElevatedCard(
            onClick = {
                vm.setExitKeyType(FocusExitKeyType.QR_CODE)
                if (config.qrCodeKey.isBlank()) {
                    vm.generateNewQrCodeKey()
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (config.exitKeyType == FocusExitKeyType.QR_CODE) colorScheme.primaryContainer else colorScheme.surfaceContainerHigh
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
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Physical QR Code Key", style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("Leave this QR code in another room to break addictive loops", style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
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
                            text = "Code: ${config.qrCodeKey}",
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

        Spacer(Modifier.height(40.dp))
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

// -------------------------------------------------------------
// 3. BAGS DRAWER CONTENT
// -------------------------------------------------------------
@Composable
private fun BagsDrawerContent(
    config: FocusConfig,
    vm: FocusViewModel,
    onPackBag: (AppBag) -> Unit,
    onClose: () -> Unit
) {
    var showCreateBagDialog by remember { mutableStateOf(false) }
    var newBagName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "App Bags",
                    style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Bundle specific apps into bags (Socials, Study, Games, etc.)",
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showCreateBagDialog = true },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Create New App Bag")
        }

        Spacer(Modifier.height(16.dp))

        config.appBags.forEach { bag ->
            val isBagActive = config.activeBlockedBags.contains(bag.id)

            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isBagActive) colorScheme.errorContainer else colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isBagActive) colorScheme.error else colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.backpack_fill0),
                                    contentDescription = null,
                                    tint = if (isBagActive) colorScheme.onError else colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = bag.name,
                                style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isBagActive) colorScheme.onErrorContainer else colorScheme.onSurface
                            )
                            Text(
                                text = "${bag.packages.size} apps inside bag",
                                style = typography.bodySmall,
                                color = if (isBagActive) colorScheme.onErrorContainer.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant
                            )
                        }

                        // Block in Focus Toggle
                        FilterChip(
                            selected = isBagActive,
                            onClick = { vm.toggleBagBlocked(bag.id) },
                            label = { Text(if (isBagActive) "Blocked" else "Allow") },
                            leadingIcon = if (isBagActive) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { vm.deleteBag(bag.id) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = colorScheme.error)
                            Spacer(Modifier.width(4.dp))
                            Text("Delete", color = colorScheme.error)
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = { onPackBag(bag) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pack Apps (${bag.packages.size})")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }

    if (showCreateBagDialog) {
        AlertDialog(
            onDismissRequest = { showCreateBagDialog = false },
            title = { Text("Create New App Bag") },
            text = {
                OutlinedTextField(
                    value = newBagName,
                    onValueChange = { newBagName = it },
                    placeholder = { Text("e.g. Socials, Study, Gaming") },
                    label = { Text("Bag Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newBagName.isNotBlank()) {
                        val newBag = AppBag(
                            id = UUID.randomUUID().toString(),
                            name = newBagName.trim(),
                            packages = emptyList()
                        )
                        vm.addOrUpdateBag(newBag)
                        newBagName = ""
                        showCreateBagDialog = false
                    }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newBagName = ""
                    showCreateBagDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 4. BAG PACKER CONTENT (Choose apps to put inside a specific bag)
// -------------------------------------------------------------
@Composable
private fun BagPackerDrawerContent(
    bag: AppBag,
    installedApps: List<AppInfo>,
    onTogglePackage: (String) -> Unit,
    onDone: () -> Unit
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
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pack \"${bag.name}\"",
                    style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurface
                )
                Text(
                    text = "${bag.packages.size} apps packed inside this bag",
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done")
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search apps to pack...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.name }) { app ->
                val isPacked = bag.packages.contains(app.name)

                ElevatedCard(
                    onClick = { onTogglePackage(app.name) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isPacked) colorScheme.primaryContainer else colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberDrawablePainter(app.icon),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                style = typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isPacked) colorScheme.onPrimaryContainer else colorScheme.onSurface
                            )
                            Text(
                                text = app.name,
                                style = typography.bodySmall,
                                color = if (isPacked) colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isPacked) colorScheme.primary else colorScheme.surfaceContainerHighest
                        ) {
                            Icon(
                                imageVector = if (isPacked) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isPacked) colorScheme.onPrimary else colorScheme.outline,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
