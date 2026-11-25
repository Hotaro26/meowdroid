package com.hotaro.meowdroid

import com.hotaro.meowdroid.feature.applications.AppGroupRepository
import com.hotaro.meowdroid.feature.focus.FocusRepository
import com.hotaro.meowdroid.feature.network.NetworkLoggingRepository
import com.hotaro.meowdroid.feature.privilege.DhizukuServerRepository
import com.hotaro.meowdroid.feature.settings.SettingsRepository
import com.hotaro.meowdroid.feature.system.SecurityLoggingRepository
import com.hotaro.meowdroid.feature.time_blocker.TimeBlockerRepository
import com.hotaro.meowdroid.feature.time_blocker.UnlockManager
import com.hotaro.meowdroid.feature.work_profile.CrossProfileIntentFilterRepository
import com.hotaro.meowdroid.utils.DhizukuError
import com.hotaro.meowdroid.utils.PrivilegeStatus
import com.hotaro.meowdroid.utils.ToastChannel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

class AppContainer(val app: MyApplication) {
    val dbHelper = MyDbHelper(app)
    val networkLoggingRepo = NetworkLoggingRepository(dbHelper)
    val securityLoggingRepo = SecurityLoggingRepository(dbHelper)
    val appGroupRepo = AppGroupRepository(dbHelper)
    val dhizukuServerRepo = DhizukuServerRepository(dbHelper)
    val cpifRepo = CrossProfileIntentFilterRepository(dbHelper)
    val timeBlockerRepo = TimeBlockerRepository(dbHelper)
    val focusRepo = FocusRepository(app.filesDir.resolve("focus_config.json"))
    val unlockManager = UnlockManager(app.filesDir.resolve("time_blocker_unlock.json"))
    val settingsRepo = SettingsRepository(app.filesDir.resolve("settings.json"))
    val dhizukuErrorState = MutableStateFlow<DhizukuError?>(null)
    val privilegeHelper = PrivilegeHelper(
        app, settingsRepo.data.privilege.dhizuku, dhizukuErrorState
    )
    val privilegeState = MutableStateFlow(PrivilegeStatus())
    val appGroupsState = MutableStateFlow(appGroupRepo.getAppGroups())
    val chosenPackage = Channel<String>(1, BufferOverflow.DROP_LATEST)
    val themeState = MutableStateFlow(settingsRepo.data.theme)
    val toastChannel = ToastChannel(app)
    val viewModelFactory = MyViewModelFactory(
        app, privilegeHelper, settingsRepo, networkLoggingRepo, dhizukuServerRepo,
        securityLoggingRepo, appGroupRepo, cpifRepo, timeBlockerRepo, focusRepo, unlockManager,
        appGroupsState, dhizukuErrorState, privilegeState, themeState, toastChannel
    )
}
