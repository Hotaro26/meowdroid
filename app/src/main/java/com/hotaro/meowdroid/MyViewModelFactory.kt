package com.hotaro.meowdroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hotaro.meowdroid.feature.applications.AppFeaturesViewModel
import com.hotaro.meowdroid.feature.applications.AppGroup
import com.hotaro.meowdroid.feature.applications.AppGroupRepository
import com.hotaro.meowdroid.feature.applications.AppGroupViewModel
import com.hotaro.meowdroid.feature.focus.FocusRepository
import com.hotaro.meowdroid.feature.focus.FocusViewModel
import com.hotaro.meowdroid.feature.network.NetworkLoggingRepository
import com.hotaro.meowdroid.feature.network.NetworkLoggingViewModel
import com.hotaro.meowdroid.feature.network.NetworkStatsViewModel
import com.hotaro.meowdroid.feature.network.NetworkViewModel
import com.hotaro.meowdroid.feature.network.OverrideApnViewModel
import com.hotaro.meowdroid.feature.network.PreferentialNetworkViewModel
import com.hotaro.meowdroid.feature.network.WifiViewModel
import com.hotaro.meowdroid.feature.password.PasswordViewModel
import com.hotaro.meowdroid.feature.privilege.DelegatedAdminsViewModel
import com.hotaro.meowdroid.feature.privilege.DhizukuServerRepository
import com.hotaro.meowdroid.feature.privilege.DhizukuServerViewModel
import com.hotaro.meowdroid.feature.privilege.TransferOwnershipViewModel
import com.hotaro.meowdroid.feature.privilege.WorkingModesViewModel
import com.hotaro.meowdroid.feature.time_blocker.TimeBlockerRepository
import com.hotaro.meowdroid.feature.time_blocker.TimeBlockerViewModel
import com.hotaro.meowdroid.feature.time_blocker.UnlockManager
import com.hotaro.meowdroid.feature.settings.MySettings
import com.hotaro.meowdroid.feature.settings.SettingsRepository
import com.hotaro.meowdroid.feature.settings.SettingsViewModel
import com.hotaro.meowdroid.feature.system.CaCertViewModel
import com.hotaro.meowdroid.feature.system.HardwareMonitorViewModel
import com.hotaro.meowdroid.feature.system.LockTaskModeViewModel
import com.hotaro.meowdroid.feature.system.SecurityLoggingRepository
import com.hotaro.meowdroid.feature.system.SecurityLoggingViewModel
import com.hotaro.meowdroid.feature.system.SystemOptionsViewModel
import com.hotaro.meowdroid.feature.system.SystemUpdateViewModel
import com.hotaro.meowdroid.feature.system.SystemViewModel
import com.hotaro.meowdroid.feature.system.TimeViewModel
import com.hotaro.meowdroid.feature.user_restriction.UserRestrictionViewModel
import com.hotaro.meowdroid.feature.users.UsersViewModel
import com.hotaro.meowdroid.feature.work_profile.CrossProfileIntentFilterRepository
import com.hotaro.meowdroid.feature.work_profile.CrossProfileIntentFilterViewModel
import com.hotaro.meowdroid.feature.work_profile.WorkProfileViewModel
import com.hotaro.meowdroid.utils.DhizukuError
import com.hotaro.meowdroid.utils.PrivilegeStatus
import com.hotaro.meowdroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

class MyViewModelFactory(
    val app: MyApplication, val ph: PrivilegeHelper,
    val sr: SettingsRepository, val nlRepo: NetworkLoggingRepository,
    val dsRepo: DhizukuServerRepository, val slRepo: SecurityLoggingRepository,
    val agRepo: AppGroupRepository, val cpifRepo: CrossProfileIntentFilterRepository,
    val tbRepo: TimeBlockerRepository, val focusRepo: FocusRepository, val um: UnlockManager,
    val agState: MutableStateFlow<List<AppGroup>>,
    val de: MutableStateFlow<DhizukuError?>, val ps: MutableStateFlow<PrivilegeStatus>,
    val ts: MutableStateFlow<MySettings.Theme>, val tc: ToastChannel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        fun checkType(cls: KClass<*>): Boolean {
            return cls.java.isAssignableFrom(modelClass)
        }
        if (checkType(NetworkStatsViewModel::class)) {
            return NetworkStatsViewModel(app, ps) as T
        }
        if (checkType(WifiViewModel::class)) {
            return WifiViewModel(app, ph, tc, ps) as T
        }
        if (checkType(OverrideApnViewModel::class)) {
            return OverrideApnViewModel(ph, tc) as T
        }
        if (checkType(PreferentialNetworkViewModel::class)) {
            return PreferentialNetworkViewModel(app, ph, tc) as T
        }
        if (checkType(NetworkLoggingViewModel::class)) {
            return NetworkLoggingViewModel(app, ph, tc, nlRepo) as T
        }
        if (checkType(NetworkViewModel::class)) {
            return NetworkViewModel(ph, tc, ps) as T
        }

        if (checkType(DelegatedAdminsViewModel::class)) {
            return DelegatedAdminsViewModel(app, ph) as T
        }
        if (checkType(TransferOwnershipViewModel::class)) {
            return TransferOwnershipViewModel(app, ph, ps) as T
        }
        if (checkType(WorkingModesViewModel::class)) {
            return WorkingModesViewModel(app, ph, sr, ps, tc, de) as T
        }
        if (checkType(DhizukuServerViewModel::class)) {
            return DhizukuServerViewModel(app, dsRepo, sr) as T
        }

        if (checkType(SecurityLoggingViewModel::class)) {
            return SecurityLoggingViewModel(app, ph, slRepo, tc) as T
        }
        if (checkType(CaCertViewModel::class)) {
            return CaCertViewModel(app, ph, tc) as T
        }
        if (checkType(LockTaskModeViewModel::class)) {
            return LockTaskModeViewModel(app, ph, tc) as T
        }
        if (checkType(SystemOptionsViewModel::class)) {
            return SystemOptionsViewModel(app, ph, sr, ps) as T
        }
        if (checkType(SystemUpdateViewModel::class)) {
            return SystemUpdateViewModel(app, ph, tc) as T
        }
        if (checkType(HardwareMonitorViewModel::class)) {
            return HardwareMonitorViewModel(app) as T
        }
        if (checkType(TimeViewModel::class)) {
            return TimeViewModel(ph, tc) as T
        }
        if (checkType(SystemViewModel::class)) {
            return SystemViewModel(app, ph, sr, ps, tc) as T
        }

        if (checkType(AppGroupViewModel::class)) {
            return AppGroupViewModel(app, agRepo, agState) as T
        }
        if (checkType(AppFeaturesViewModel::class)) {
            return AppFeaturesViewModel(app, ph, ps, tc, sr) as T
        }

        if (checkType(WorkProfileViewModel::class)) {
            return WorkProfileViewModel(ph, ps, tc) as T
        }
        if (checkType(CrossProfileIntentFilterViewModel::class)) {
            return CrossProfileIntentFilterViewModel(app, ph, cpifRepo, tc, sr) as T
        }

        if (checkType(UserRestrictionViewModel::class)) {
            return UserRestrictionViewModel(app, ph, sr, ps, tc) as T
        }

        if (checkType(UsersViewModel::class)) {
            return UsersViewModel(app, ph, tc, sr, ps) as T
        }

        if (checkType(PasswordViewModel::class)) {
            return PasswordViewModel(app, ph, sr, ps, tc) as T
        }

        if (checkType(TimeBlockerViewModel::class)) {
            return TimeBlockerViewModel(app, ph, tbRepo, tc, ps) as T
        }

        if (checkType(FocusViewModel::class)) {
            return FocusViewModel(app, ph, focusRepo, tc) as T
        }

        if (checkType(SettingsViewModel::class)) {
            return SettingsViewModel(app, sr, ph, ps, tc, ts, um, tbRepo) as T
        }
        throw Exception("Unknown ViewModel")
    }
}
