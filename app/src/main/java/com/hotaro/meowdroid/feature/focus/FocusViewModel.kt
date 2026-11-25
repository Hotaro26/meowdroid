package com.hotaro.meowdroid.feature.focus

import android.app.Activity
import android.app.ActivityOptions
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotaro.meowdroid.MyApplication
import com.hotaro.meowdroid.PrivilegeHelper
import com.hotaro.meowdroid.R
import com.hotaro.meowdroid.utils.AppInfo
import com.hotaro.meowdroid.utils.ToastChannel
import com.hotaro.meowdroid.utils.getAppInfo
import com.hotaro.meowdroid.utils.hash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FocusViewModel(
    val application: MyApplication,
    val ph: PrivilegeHelper,
    val repo: FocusRepository,
    val toastChannel: ToastChannel
) : ViewModel() {

    private val _configState = MutableStateFlow(repo.config)
    val configState = _configState.asStateFlow()

    private val _sessionState = MutableStateFlow(FocusSessionState())
    val sessionState = _sessionState.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = application.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val launchable = packages.filter { app ->
                pm.getLaunchIntentForPackage(app.packageName) != null &&
                        app.packageName != application.packageName
            }.map { getAppInfo(pm, it) }.sortedBy { it.label.lowercase() }
            _installedApps.value = launchable
        }
    }

    fun setDuration(minutes: Int) {
        val safeMin = minutes.coerceIn(1, 480)
        repo.update { it.copy(durationMinutes = safeMin) }
        _configState.value = repo.config
    }

    fun setPassword(rawPassword: String) {
        val hash = if (rawPassword.isBlank()) "" else rawPassword.hash()
        repo.update { it.copy(passwordHash = hash) }
        _configState.value = repo.config
    }

    fun setQrCodeKey(key: String) {
        repo.update { it.copy(qrCodeKey = key) }
        _configState.value = repo.config
    }

    fun generateNewQrCodeKey(): String {
        val newKey = "MEOW-" + UUID.randomUUID().toString().take(8).uppercase()
        setQrCodeKey(newKey)
        return newKey
    }

    fun setExitKeyType(type: FocusExitKeyType) {
        repo.update { it.copy(exitKeyType = type) }
        _configState.value = repo.config
    }

    fun toggleBlockedPackage(packageName: String) {
        val current = repo.config.blockedPackages.toMutableList()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        repo.update { it.copy(blockedPackages = current) }
        _configState.value = repo.config
    }

    fun setStrictMode(enabled: Boolean) {
        repo.update { it.copy(strictMode = enabled) }
        _configState.value = repo.config
    }

    fun setUseKioskLock(enabled: Boolean) {
        repo.update { it.copy(useKioskLock = enabled) }
        _configState.value = repo.config
    }

    fun setSuspendOtherApps(enabled: Boolean) {
        repo.update { it.copy(suspendOtherApps = enabled) }
        _configState.value = repo.config
    }

    fun setAllowNotifications(allow: Boolean) {
        repo.update { it.copy(allowNotifications = allow) }
        _configState.value = repo.config
    }

    fun toggleAllowedPackage(packageName: String) {
        val current = repo.config.allowedPackages.toMutableList()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        repo.update { it.copy(allowedPackages = current) }
        _configState.value = repo.config
    }

    fun startMonkMode(activity: Activity) {
        val config = repo.config
        val totalSecs = config.durationMinutes * 60L
        val now = System.currentTimeMillis()
        val endTime = now + (totalSecs * 1000L)
        val quote = MINDFUL_QUOTES.random()

        ph.safeDpmCall {
            try {
                val allowedPackages = (config.allowedPackages + application.packageName).distinct()
                var suspendedList = emptyList<String>()

                // Suspend blocked apps (or non-allowed apps)
                if (Build.VERSION.SDK_INT >= 24) {
                    val toSuspend = if (config.blockedPackages.isNotEmpty()) {
                        config.blockedPackages.filter { !isCriticalPackage(it) }
                    } else if (config.suspendOtherApps) {
                        _installedApps.value.map { it.name }.filter { pkg ->
                            !allowedPackages.contains(pkg) && !isCriticalPackage(pkg)
                        }
                    } else {
                        emptyList()
                    }

                    if (toSuspend.isNotEmpty()) {
                        dpm.setPackagesSuspended(dar, toSuspend.toTypedArray(), true)
                        suspendedList = toSuspend
                    }
                }

                // LockTask Kiosk mode if option is enabled
                if (config.useKioskLock) {
                    dpm.setLockTaskPackages(dar, allowedPackages.toTypedArray())

                    if (Build.VERSION.SDK_INT >= 28) {
                        var features = DevicePolicyManager.LOCK_TASK_FEATURE_HOME or
                                DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW
                        if (config.allowNotifications) {
                            features = features or DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS
                        }
                        dpm.setLockTaskFeatures(dar, features)
                    }

                    activity.startLockTask()
                }

                _sessionState.value = FocusSessionState(
                    state = FocusState.ACTIVE,
                    startTimeEpochMs = now,
                    endTimeEpochMs = endTime,
                    totalSeconds = totalSecs,
                    remainingSeconds = totalSecs,
                    currentQuote = quote,
                    suspendedPackages = suspendedList
                )

                startTimer(activity)
                toastChannel.sendText("Focus mode started")
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendText("Failed to start focus mode")
            }
        }
    }

    private fun startTimer(activity: Activity) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentSession = _sessionState.value
                if (currentSession.state != FocusState.ACTIVE) break

                val now = System.currentTimeMillis()
                val remainingMs = currentSession.endTimeEpochMs - now
                val remainingSec = (remainingMs / 1000).coerceAtLeast(0)

                _sessionState.value = currentSession.copy(remainingSeconds = remainingSec)

                if (remainingSec <= 0) {
                    endMonkMode(activity, force = true)
                    break
                }
            }
        }
    }

    fun endMonkMode(activity: Activity, force: Boolean = false, passwordAttempt: String? = null): Boolean {
        val config = repo.config
        val session = _sessionState.value

        if (!force && session.remainingSeconds > 0) {
            if (config.strictMode) {
                toastChannel.sendText("Strict mode active: Cannot end focus early")
                return false
            }

            when (config.exitKeyType) {
                FocusExitKeyType.PASSWORD -> {
                    if (config.passwordHash.isNotBlank()) {
                        if (passwordAttempt == null || passwordAttempt.hash() != config.passwordHash) {
                            toastChannel.sendText("Incorrect password")
                            return false
                        }
                    }
                }
                FocusExitKeyType.QR_CODE -> {
                    if (config.qrCodeKey.isNotBlank()) {
                        if (passwordAttempt != config.qrCodeKey) {
                            toastChannel.sendText("Incorrect QR code key")
                            return false
                        }
                    }
                }
                FocusExitKeyType.NONE -> {
                    // No key required
                }
            }
        }

        timerJob?.cancel()
        timerJob = null

        ph.safeDpmCall {
            try {
                if (config.useKioskLock) {
                    activity.stopLockTask()
                }

                if (session.suspendedPackages.isNotEmpty() && Build.VERSION.SDK_INT >= 24) {
                    dpm.setPackagesSuspended(dar, session.suspendedPackages.toTypedArray(), false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        _sessionState.value = FocusSessionState(state = FocusState.IDLE)
        toastChannel.sendText("Focus session completed")
        return true
    }

    fun launchAllowedApp(context: Context, packageName: String) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val options = ActivityOptions.makeBasic()
                if (Build.VERSION.SDK_INT >= 28) {
                    options.setLockTaskEnabled(true)
                }
                context.startActivity(intent, options.toBundle())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isCriticalPackage(packageName: String): Boolean {
        val critical = listOf(
            "com.android.phone",
            "com.android.server.telecom",
            "com.google.android.dialer",
            "com.android.dialer",
            "com.android.emergency",
            "com.google.android.emergency",
            "com.android.settings",
            "com.android.systemui"
        )
        return critical.contains(packageName)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
