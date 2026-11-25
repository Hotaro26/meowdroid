package com.bintianqi.owndroid.feature.focus

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.ToastChannel
import com.bintianqi.owndroid.utils.getAppInfo
import com.bintianqi.owndroid.utils.hash
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
        val quote = MINDFUL_QUOTES.random()

        ph.safeDpmCall {
            try {
                val packagesToAllow = (config.allowedPackages + application.packageName).distinct().toTypedArray()
                dpm.setLockTaskPackages(dar, packagesToAllow)

                if (Build.VERSION.SDK_INT >= 28) {
                    val features = if (config.allowNotifications) {
                        DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS or DevicePolicyManager.LOCK_TASK_FEATURE_HOME
                    } else {
                        DevicePolicyManager.LOCK_TASK_FEATURE_NONE
                    }
                    dpm.setLockTaskFeatures(dar, features)
                }

                activity.startLockTask()

                _sessionState.value = FocusSessionState(
                    state = FocusState.ACTIVE,
                    totalSeconds = totalSecs,
                    remainingSeconds = totalSecs,
                    currentQuote = quote
                )

                startTimer(activity)
                toastChannel.sendText(R.string.start_monk_mode)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendText(R.string.failed)
            }
        }
    }

    private fun startTimer(activity: Activity) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Main) {
            var remaining = _sessionState.value.remainingSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining -= 1
                _sessionState.value = _sessionState.value.copy(remainingSeconds = remaining)
            }
            // Timer expired!
            stopLockTaskSafely(activity)
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
        stopLockTaskSafely(activity)
        _sessionState.value = FocusSessionState(state = FocusState.IDLE)
        onResult(true, null)
    }

    fun resetSession() {
        _sessionState.value = FocusSessionState(state = FocusState.IDLE)
    }

    private fun stopLockTaskSafely(activity: Activity) {
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchApp(context: Context, packageName: String) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                toastChannel.sendText(R.string.not_exist)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toastChannel.sendText(R.string.failed)
        }
    }
}
