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

                // 1. Suspend non-allowed apps if option is enabled
                if (config.suspendOtherApps && Build.VERSION.SDK_INT >= 24) {
                    val pm = application.packageManager
                    val toSuspend = _installedApps.value.map { it.name }.filter { pkg ->
                        !allowedPackages.contains(pkg) && !isCriticalPackage(pkg)
                    }
                    if (toSuspend.isNotEmpty()) {
                        dpm.setPackagesSuspended(dar, toSuspend.toTypedArray(), true)
                        suspendedList = toSuspend
                    }
                }

                // 2. LockTask Kiosk mode if option is enabled
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
                toastChannel.sendText(R.string.start_monk_mode)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendText(R.string.failed)
            }
        }
    }

    private fun isCriticalPackage(packageName: String): Boolean {
        return packageName.contains("telecom") ||
                packageName.contains("dialer") ||
                packageName.contains("emergency") ||
                packageName.contains("systemui") ||
                packageName.contains("android.settings") ||
                packageName == application.packageName
    }

    private fun startTimer(activity: Activity) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Main) {
            val endTime = _sessionState.value.endTimeEpochMs
            while (true) {
                val currentTime = System.currentTimeMillis()
                val remaining = ((endTime - currentTime + 999L) / 1000L).coerceAtLeast(0L)
                _sessionState.value = _sessionState.value.copy(remainingSeconds = remaining)

                if (remaining <= 0L) {
                    break
                }
                delay(1000L)
            }

            // Session completed naturally!
            finishSessionCleanup(activity)
            _sessionState.value = _sessionState.value.copy(
                state = FocusState.COMPLETED,
                remainingSeconds = 0L
            )
            toastChannel.sendText(R.string.session_completed_title)
        }
    }

    fun endMonkMode(
        activity: Activity,
        enteredPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val config = repo.config
        val currentSession = _sessionState.value

        if (config.strictMode && currentSession.remainingSeconds > 0) {
            onResult(false, application.getString(R.string.strict_mode_warning))
            return
        }

        if (config.passwordHash.isNotEmpty()) {
            if (enteredPassword.hash() != config.passwordHash) {
                onResult(false, application.getString(R.string.wrong_password))
                return
            }
        }

        timerJob?.cancel()
        finishSessionCleanup(activity)
        _sessionState.value = FocusSessionState(state = FocusState.IDLE)
        onResult(true, null)
    }

    private fun finishSessionCleanup(activity: Activity) {
        val suspended = _sessionState.value.suspendedPackages
        ph.safeDpmCall {
            try {
                if (suspended.isNotEmpty() && Build.VERSION.SDK_INT >= 24) {
                    dpm.setPackagesSuspended(dar, suspended.toTypedArray(), false)
                }
                if (_configState.value.useKioskLock) {
                    activity.stopLockTask()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetSession() {
        _sessionState.value = FocusSessionState(state = FocusState.IDLE)
    }

    fun launchApp(context: Context, packageName: String) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (Build.VERSION.SDK_INT >= 28 && _configState.value.useKioskLock) {
                    val options = ActivityOptions.makeBasic().setLockTaskEnabled(true)
                    context.startActivity(intent, options.toBundle())
                } else {
                    context.startActivity(intent)
                }
            } else {
                toastChannel.sendText(R.string.not_exist)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toastChannel.sendText(R.string.failed)
        }
    }
}
