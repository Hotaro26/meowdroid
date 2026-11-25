package com.hotaro.meowdroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.hotaro.meowdroid.AppContainer
import com.hotaro.meowdroid.R
import com.hotaro.meowdroid.feature.applications.AppChooserFilter
import com.hotaro.meowdroid.feature.applications.AppChooserMode
import com.hotaro.meowdroid.feature.applications.AppChooserScreen
import com.hotaro.meowdroid.feature.applications.AppChooserViewModel
import com.hotaro.meowdroid.feature.applications.AppDetailsViewModel
import com.hotaro.meowdroid.feature.applications.AppFeaturesViewModel
import com.hotaro.meowdroid.feature.applications.AppFilterState
import com.hotaro.meowdroid.feature.applications.AppGroupsScreen
import com.hotaro.meowdroid.feature.applications.AppPermissionsManagerScreen
import com.hotaro.meowdroid.feature.applications.ApplicationDetailsScreen
import com.hotaro.meowdroid.feature.applications.ApplicationsFeaturesScreen
import com.hotaro.meowdroid.feature.applications.ClearAppStorageScreen
import com.hotaro.meowdroid.feature.applications.CredentialManagerPolicyScreen
import com.hotaro.meowdroid.feature.applications.EditAppGroupScreen
import com.hotaro.meowdroid.feature.applications.EnableSystemAppScreen
import com.hotaro.meowdroid.feature.applications.InstallExistingAppScreen
import com.hotaro.meowdroid.feature.applications.ManagedConfigurationBooleanEditorScreen
import com.hotaro.meowdroid.feature.applications.ManagedConfigurationListEditorScreen
import com.hotaro.meowdroid.feature.applications.ManagedConfigurationScreen
import com.hotaro.meowdroid.feature.applications.ManagedConfigurationValueEditorScreen
import com.hotaro.meowdroid.feature.applications.ManagedConfigurationViewModel
import com.hotaro.meowdroid.feature.applications.PackageFunctionScreen
import com.hotaro.meowdroid.feature.applications.PermissionDetailScreen
import com.hotaro.meowdroid.feature.applications.PermissionManagerScreen
import com.hotaro.meowdroid.feature.applications.PermittedAsAndImPackagesScreen
import com.hotaro.meowdroid.feature.applications.SetDefaultDialerScreen
import com.hotaro.meowdroid.feature.applications.UninstallAppScreen
import com.hotaro.meowdroid.feature.focus.FocusViewModel
import com.hotaro.meowdroid.feature.focus.ZenSettingsScreen
import com.hotaro.meowdroid.feature.network.AddApnSettingScreen
import com.hotaro.meowdroid.feature.network.AddPreferentialNetworkServiceConfigScreen
import com.hotaro.meowdroid.feature.network.AlwaysOnVpnPackageScreen
import com.hotaro.meowdroid.feature.network.NetworkLoggingScreen
import com.hotaro.meowdroid.feature.network.NetworkOptionsScreen
import com.hotaro.meowdroid.feature.network.NetworkScreen
import com.hotaro.meowdroid.feature.network.NetworkStatsScreen
import com.hotaro.meowdroid.feature.network.NetworkStatsViewerScreen
import com.hotaro.meowdroid.feature.network.OverrideApnScreen
import com.hotaro.meowdroid.feature.network.PreferentialNetworkServiceScreen
import com.hotaro.meowdroid.feature.network.PrivateDnsScreen
import com.hotaro.meowdroid.feature.network.RecommendedGlobalProxyScreen
import com.hotaro.meowdroid.feature.network.UpdateNetworkScreen
import com.hotaro.meowdroid.feature.network.WifiScreen
import com.hotaro.meowdroid.feature.network.WifiSecurityLevelScreen
import com.hotaro.meowdroid.feature.network.WifiSsidPolicyScreen
import com.hotaro.meowdroid.feature.password.KeyguardDisabledFeaturesScreen
import com.hotaro.meowdroid.feature.password.PasswordInfoScreen
import com.hotaro.meowdroid.feature.password.PasswordScreen
import com.hotaro.meowdroid.feature.password.RequiredPasswordComplexityScreen
import com.hotaro.meowdroid.feature.password.RequiredPasswordQualityScreen
import com.hotaro.meowdroid.feature.password.ResetPasswordScreen
import com.hotaro.meowdroid.feature.password.ResetPasswordTokenScreen
import com.hotaro.meowdroid.feature.privilege.AddDelegatedAdminScreen
import com.hotaro.meowdroid.feature.privilege.DelegatedAdminsScreen
import com.hotaro.meowdroid.feature.privilege.DhizukuServerSettingsScreen
import com.hotaro.meowdroid.feature.privilege.TransferOwnershipScreen
import com.hotaro.meowdroid.feature.privilege.WorkModesScreen
import com.hotaro.meowdroid.feature.settings.AboutScreen
import com.hotaro.meowdroid.feature.settings.ApiSettings
import com.hotaro.meowdroid.feature.settings.AppLockSettingsScreen
import com.hotaro.meowdroid.feature.settings.AppearanceScreen
import com.hotaro.meowdroid.feature.settings.ExportSettingsScreen
import com.hotaro.meowdroid.feature.settings.ImportSettingsScreen
import com.hotaro.meowdroid.feature.settings.NotificationsScreen
import com.hotaro.meowdroid.feature.settings.SettingsOptionsScreen
import com.hotaro.meowdroid.feature.settings.SettingsScreen
import com.hotaro.meowdroid.feature.settings.SettingsSyncScreen
import com.hotaro.meowdroid.feature.settings.SettingsViewModel
import com.hotaro.meowdroid.feature.settings.ShortcutSettingsScreen
import com.hotaro.meowdroid.feature.system.CaCertScreen
import com.hotaro.meowdroid.feature.system.ContentProtectionPolicyScreen
import com.hotaro.meowdroid.feature.system.DefaultInputMethodScreen
import com.hotaro.meowdroid.feature.system.DeviceInfoScreen
import com.hotaro.meowdroid.feature.system.DisableAccountManagementScreen
import com.hotaro.meowdroid.feature.system.FrpPolicyScreen
import com.hotaro.meowdroid.feature.system.HardwareMonitorScreen
import com.hotaro.meowdroid.feature.system.KeyguardScreen
import com.hotaro.meowdroid.feature.system.LockScreenInfoScreen
import com.hotaro.meowdroid.feature.system.LockTaskModeScreen
import com.hotaro.meowdroid.feature.system.MtePolicyScreen
import com.hotaro.meowdroid.feature.system.NearbyStreamingPolicyScreen
import com.hotaro.meowdroid.feature.system.PermissionPolicyScreen
import com.hotaro.meowdroid.feature.system.SecurityLoggingScreen
import com.hotaro.meowdroid.feature.system.SupportMessageScreen
import com.hotaro.meowdroid.feature.system.SystemOptionsScreen
import com.hotaro.meowdroid.feature.system.SystemScreen
import com.hotaro.meowdroid.feature.system.SystemUpdateScreen
import com.hotaro.meowdroid.feature.system.TimeScreen
import com.hotaro.meowdroid.feature.system.WipeDataScreen
import com.hotaro.meowdroid.feature.time_blocker.TimeBlockerEditScreen
import com.hotaro.meowdroid.feature.time_blocker.TimeBlockerScreen
import com.hotaro.meowdroid.feature.user_restriction.UserRestrictionEditorScreen
import com.hotaro.meowdroid.feature.user_restriction.UserRestrictionOptionsScreen
import com.hotaro.meowdroid.feature.user_restriction.UserRestrictionScreen
import com.hotaro.meowdroid.feature.users.AffiliationIdScreen
import com.hotaro.meowdroid.feature.users.ChangeUsernameScreen
import com.hotaro.meowdroid.feature.users.CreateUserScreen
import com.hotaro.meowdroid.feature.users.UserInfoScreen
import com.hotaro.meowdroid.feature.users.UserOperationScreen
import com.hotaro.meowdroid.feature.users.UserSessionMessageScreen
import com.hotaro.meowdroid.feature.users.UsersOptionsScreen
import com.hotaro.meowdroid.feature.users.UsersScreen
import com.hotaro.meowdroid.feature.work_profile.AddCrossProfileIntentFilterScreen
import com.hotaro.meowdroid.feature.work_profile.CreateWorkProfileScreen
import com.hotaro.meowdroid.feature.work_profile.CrossProfileIntentFilterPresetsScreen
import com.hotaro.meowdroid.feature.work_profile.CrossProfileIntentFilterScreen
import com.hotaro.meowdroid.feature.work_profile.DeleteWorkProfileScreen
import com.hotaro.meowdroid.feature.work_profile.SuspendPersonalAppScreen
import com.hotaro.meowdroid.feature.work_profile.WorkProfileScreen
import com.hotaro.meowdroid.ui.screen.HomeScreen
import com.hotaro.meowdroid.utils.viewModelFactory

/**
 * Nav3 provides a frequently-changing content key factory, which has broken our app several times,
 * so I prefer to use a custom and stable content key factory.
 */
inline fun <reified K: Destination> EntryProviderScope<Destination>.myEntry(
    metadata: Map<String, Any> = emptyMap(), noinline content: @Composable ((K) -> Unit)
) {
    entry({ it::class.qualifiedName!! }, metadata, content)
}

@Suppress("NewApi")
fun myEntryProvider(
    backstack: NavBackStack<NavKey>, appChooserVm: AppChooserViewModel,
    container: AppContainer
) = entryProvider {
    fun navigate(dest: Destination) {
        backstack += dest
    }

    fun navigateUp() {
        if (backstack.size > 1) backstack.removeLastOrNull()
    }

    fun navigateToAppGroups() {
        navigate(Destination.AppGroups)
    }

    fun navigateAndPopAll(dest: Destination) {
        navigate(dest)
        repeat(backstack.size - 1) {
            backstack.removeFirstOrNull()
        }
    }

    fun choosePackage() {
        navigate(Destination.ApplicationsList(AppChooserMode.Choose))
    }

    fun chooseSinglePackage() {
        navigate(Destination.ApplicationsList(AppChooserMode.SingleChoose))
    }

    myEntry<Destination.Home> {
        HomeScreen(
            container.privilegeState,
            viewModel(factory = container.viewModelFactory),
            viewModel(factory = container.viewModelFactory),
            ::navigate
        )
    }
    myEntry<Destination.WorkingModes> {
        WorkModesScreen(viewModel(factory = container.viewModelFactory), it, ::navigateUp, {
            navigateAndPopAll(Destination.Home)
        }, {
            navigateAndPopAll(Destination.WorkingModes(false))
        }, ::navigate)
    }
    myEntry<Destination.DhizukuServerSettings> {
        DhizukuServerSettingsScreen(viewModel(factory = container.viewModelFactory), ::navigateUp)
    }

    myEntry<Destination.DelegatedAdmins> {
        DelegatedAdminsScreen(
            viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate
        )
    }
    myEntry<Destination.DelegatedAdminDetails>(
        metadata = navParentKey<Destination.DelegatedAdmins>()
    ) {
        AddDelegatedAdminScreen(
            viewModel(), container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.DeviceInfo>(
        metadata = navParentKey<Destination.System>()
    ) {
        DeviceInfoScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.LockScreenInfo>(
        metadata = navParentKey<Destination.System>()
    ) {
        LockScreenInfoScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.SupportMessage>(
        metadata = navParentKey<Destination.System>()
    ) {
        SupportMessageScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.TransferOwnership> {
        TransferOwnershipScreen(
            viewModel(factory = container.viewModelFactory), ::navigateUp
        ) {
            navigate(Destination.WorkingModes(false))
            while (backstack.size > 1) {
                backstack.removeFirstOrNull()
            }
        }
    }

    myEntry<Destination.System> {
        SystemScreen(viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate)
    }
    myEntry<Destination.SystemOptions> {
        SystemOptionsScreen(viewModel(factory = container.viewModelFactory), ::navigateUp)
    }
    myEntry<Destination.Keyguard>(
        metadata = navParentKey<Destination.System>()
    ) {
        KeyguardScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.HardwareMonitor> {
        HardwareMonitorScreen(viewModel(factory = container.viewModelFactory), ::navigateUp)
    }
    myEntry<Destination.DefaultInputMethod>(
        metadata = navParentKey<Destination.System>()
    ) {
        DefaultInputMethodScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.Time> {
        TimeScreen(viewModel(factory = container.viewModelFactory), ::navigateUp)
    }
    myEntry<Destination.ContentProtectionPolicy>(
        metadata = navParentKey<Destination.System>()
    ) {
        ContentProtectionPolicyScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.PermissionPolicy>(
        metadata = navParentKey<Destination.System>()
    ) {
        PermissionPolicyScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.MtePolicy>(
        metadata = navParentKey<Destination.System>()
    ) {
        MtePolicyScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.NearbyStreamingPolicy>(
        metadata = navParentKey<Destination.System>()
    ) {
        NearbyStreamingPolicyScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.LockTaskMode> {
        LockTaskModeScreen(
            viewModel(factory = container.viewModelFactory),
            container.chosenPackage, ::chooseSinglePackage, ::choosePackage, ::navigateUp
        )
    }
    myEntry<Destination.CaCert> {
        CaCertScreen(viewModel(factory = container.viewModelFactory), ::navigateUp)
    }
    myEntry<Destination.SecurityLogging> {
        SecurityLoggingScreen(viewModel(factory = container.viewModelFactory), ::navigateUp)
    }
    myEntry<Destination.DisableAccountManagement>(
        metadata = navParentKey<Destination.System>()
    ) {
        DisableAccountManagementScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.SystemUpdate> {
        SystemUpdateScreen(viewModel(factory = container.viewModelFactory), ::navigateUp)
    }
    myEntry<Destination.FrpPolicy>(
        metadata = navParentKey<Destination.System>()
    ) {
        FrpPolicyScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.WipeData>(
        metadata = navParentKey<Destination.System>()
    ) { WipeDataScreen(viewModel(), ::navigateUp) }

    myEntry<Destination.Network> {
        NetworkScreen(viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate)
    }
    myEntry<Destination.WiFi> {
        WifiScreen(
            viewModel(factory = container.viewModelFactory),
            ::navigate, ::navigateUp
        )
    }
    myEntry<Destination.UpdateNetwork>(
        metadata = navParentKey<Destination.WiFi>()
    ) {
        UpdateNetworkScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.WifiSecurityLevel>(
        metadata = navParentKey<Destination.WiFi>()
    ) {
        WifiSecurityLevelScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.WifiSsidPolicy>(
        metadata = navParentKey<Destination.WiFi>()
    ) {
        WifiSsidPolicyScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.NetworkOptions>(
        metadata = navParentKey<Destination.Network>()
    ) {
        NetworkOptionsScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.NetworkStats> {
        NetworkStatsScreen(
            viewModel(factory = container.viewModelFactory),
            ::chooseSinglePackage, container.chosenPackage, ::navigateUp
        ) {
            navigate(Destination.NetworkStatsViewer)
        }
    }
    myEntry<Destination.NetworkStatsViewer>(
        metadata = navParentKey<Destination.NetworkStats>()
    ) {
        NetworkStatsViewerScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.PrivateDns>(
        metadata = navParentKey<Destination.Network>()
    ) {
        PrivateDnsScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.AlwaysOnVpnPackage>(
        metadata = navParentKey<Destination.Network>()
    ) {
        AlwaysOnVpnPackageScreen(
            viewModel(), container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.RecommendedGlobalProxy>(
        metadata = navParentKey<Destination.Network>()
    ) {
        RecommendedGlobalProxyScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.NetworkLogging> {
        NetworkLoggingScreen(viewModel(factory = container.viewModelFactory), ::navigateUp)
    }
    //entry<Destination.WifiAuthKeypair> { WifiAuthKeypairScreen(::navigateUp) }
    myEntry<Destination.PreferentialNetworkService> {
        PreferentialNetworkServiceScreen(
            viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate
        )
    }
    myEntry<Destination.AddPreferentialNetworkServiceConfig>(
        metadata = navParentKey<Destination.PreferentialNetworkService>()
    ) {
        AddPreferentialNetworkServiceConfigScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.OverrideApn> {
        OverrideApnScreen(
            viewModel(factory = container.viewModelFactory), ::navigateUp
        ) { navigate(Destination.AddApnSetting) }
    }
    myEntry<Destination.AddApnSetting>(
        metadata = navParentKey<Destination.OverrideApn>()
    ) {
        AddApnSettingScreen(viewModel(), ::navigateUp)
    }

    myEntry<Destination.WorkProfile> {
        WorkProfileScreen(viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate)
    }
    myEntry<Destination.CreateWorkProfile> {
        CreateWorkProfileScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.SuspendPersonalApp>(
        metadata = navParentKey<Destination.WorkProfile>()
    ) {
        SuspendPersonalAppScreen(
            viewModel(), ::navigateUp
        )
    }
    myEntry<Destination.CrossProfileIntentFilter> {
        CrossProfileIntentFilterScreen(
            viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate
        )
    }
    myEntry<Destination.AddCrossProfileIntentFilter>(
        metadata = navParentKey<Destination.CrossProfileIntentFilter>()
    ) {
        AddCrossProfileIntentFilterScreen(it, viewModel(), ::navigateUp)
    }
    myEntry<Destination.CrossProfileIntentFilterPresets>(
        metadata = navParentKey<Destination.CrossProfileIntentFilter>()
    ) {
        CrossProfileIntentFilterPresetsScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.DeleteWorkProfile>(
        metadata = navParentKey<Destination.WorkProfile>()
    ) {
        DeleteWorkProfileScreen(viewModel(), ::navigateUp)
    }

    myEntry<Destination.ApplicationsList> { params ->
        AppChooserScreen(
            params, appChooserVm
        ) { name ->
            if (params.mode == AppChooserMode.ListView) {
                if (name != null) {
                    navigate(Destination.ApplicationDetails(name))
                } else {
                    navigateUp()
                }
            } else {
                if (name != null) container.chosenPackage.trySend(name)
                navigateUp()
            }
        }
    }

    myEntry<Destination.ApplicationFeatures> {
        ApplicationsFeaturesScreen(
            viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate
        )
    }
    myEntry<Destination.ApplicationDetails> {
        ApplicationDetailsScreen(
            viewModel(
                factory = viewModelFactory {
                    AppDetailsViewModel(
                        it.packageName, container.app, container.privilegeHelper,
                        container.privilegeState, container.toastChannel
                    )
                }
            ), ::navigateUp, ::navigate
        )
    }
    myEntry<Destination.Suspend>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PackageFunctionScreen(
            R.string.suspend, vm.suspendedPackages, vm::getSuspendedPackages,
            vm::setPackageSuspended, ::navigateUp, container.chosenPackage, ::choosePackage,
            ::navigateToAppGroups, container.appGroupsState, R.string.info_suspend_app,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.Hide>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PackageFunctionScreen(
            R.string.hide, vm.hiddenPackages, vm::getHiddenPackages, vm::setPackageHidden,
            ::navigateUp, container.chosenPackage, ::choosePackage, ::navigateToAppGroups,
            container.appGroupsState, null, vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.BlockUninstall>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PackageFunctionScreen(
            R.string.block_uninstall, vm.ubPackages, vm::getUbPackages, vm::setPackageUb,
            ::navigateUp, container.chosenPackage, ::choosePackage, ::navigateToAppGroups,
            container.appGroupsState, null, vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.DisableUserControl>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PackageFunctionScreen(
            R.string.disable_user_control, vm.ucdPackages, vm::getUcdPackages,
            vm::setPackageUcd, ::navigateUp, container.chosenPackage, ::choosePackage,
            ::navigateToAppGroups, container.appGroupsState, R.string.info_disable_user_control,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.AppPermissionsManager>(
        metadata = navParentKey<Destination.ApplicationDetails>()
    ) {
        AppPermissionsManagerScreen(
            viewModel(), ::navigateUp
        )
    }
    myEntry<Destination.PermissionManager>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        PermissionManagerScreen(viewModel(), ::navigate, ::navigateUp)
    }
    myEntry<Destination.PermissionDetail>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        PermissionDetailScreen(
            viewModel(), ::navigateUp
        )
    }
    myEntry<Destination.DisableMeteredData>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PackageFunctionScreen(
            R.string.disable_metered_data, vm.mddPackages, vm::getMddPackages,
            vm::setPackageMdd, ::navigateUp, container.chosenPackage, ::choosePackage,
            ::navigateToAppGroups, container.appGroupsState, null,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting,
            AppChooserFilter(usesInternet = true)
        )
    }
    myEntry<Destination.ClearAppStorage>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        ClearAppStorageScreen(
            viewModel(), container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.UninstallApp>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        UninstallAppScreen(
            vm, container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.KeepUninstalledPackages>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PackageFunctionScreen(
            R.string.keep_uninstalled_packages, vm.kuPackages, vm::getKuPackages,
            vm::setPackageKu, ::navigateUp, container.chosenPackage, ::choosePackage,
            ::navigateToAppGroups, container.appGroupsState, R.string.info_keep_uninstalled_apps,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.InstallExistingApp>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        InstallExistingAppScreen(
            vm, container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.CrossProfilePackages>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PackageFunctionScreen(
            R.string.cross_profile_apps, vm.cpPackages,
            vm::getCpPackages, vm::setPackageCp, ::navigateUp, container.chosenPackage,
            ::choosePackage, ::navigateToAppGroups, container.appGroupsState, null,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.CrossProfileWidgetProviders>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PackageFunctionScreen(
            R.string.cross_profile_widget, vm.cpwProviders,
            vm::getCpwProviders, vm::setCpwProvider, ::navigateUp, container.chosenPackage,
            ::choosePackage, ::navigateToAppGroups, container.appGroupsState, null,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.CredentialManagerPolicy>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        CredentialManagerPolicyScreen(
            vm, container.chosenPackage, ::choosePackage, ::navigateUp
        )
    }
    myEntry<Destination.PermittedAccessibilityServices>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PermittedAsAndImPackagesScreen(
            R.string.permitted_accessibility_services,
            R.string.system_accessibility_always_allowed, container.chosenPackage, ::choosePackage,
            vm.pasAllowAll, vm.pasPackages, vm::getPasPolicy, vm::setPasAllowAll, vm::setPasPackage,
            vm::applyPasPolicy, ::navigateUp
        )
    }
    myEntry<Destination.PermittedInputMethods>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        PermittedAsAndImPackagesScreen(
            R.string.permitted_ime, R.string.system_ime_always_allowed,
            container.chosenPackage, ::choosePackage, vm.pimAllowAll, vm.pimPackages,
            vm::getPimPolicy, vm::setPimAllowAll,
            vm::setPimPackage, vm::applyPimPolicy, ::navigateUp
        )
    }
    myEntry<Destination.EnableSystemApp>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        EnableSystemAppScreen(
            container.chosenPackage, {
                navigate(
                    Destination.ApplicationsList(
                        AppChooserMode.SingleChoose, AppChooserFilter(
                            userApps = AppFilterState.No, installed = AppFilterState.No
                        )
                    )
                )
            }, vm::enableSystemApp, ::navigateUp
        )
    }
    myEntry<Destination.SetDefaultDialer>(
        metadata = navParentKey<Destination.ApplicationFeatures>()
    ) {
        val vm = viewModel<AppFeaturesViewModel>()
        SetDefaultDialerScreen(
            container.chosenPackage, ::chooseSinglePackage, vm::setDefaultDialer, ::navigateUp
        )
    }
    myEntry<Destination.ManagedConfiguration> {
        ManagedConfigurationScreen(
            viewModel(factory = viewModelFactory {
                ManagedConfigurationViewModel(
                    it.packageName, container.app, container.privilegeHelper, container.toastChannel
                )
            }), ::navigateUp, ::navigate
        )
    }
    myEntry<Destination.ManagedConfigurationValueEditor>(
        metadata = navParentKey<Destination.ManagedConfiguration>()
    ) {
        ManagedConfigurationValueEditorScreen(viewModel(), it.id, it.isInt, ::navigateUp)
    }
    myEntry<Destination.ManagedConfigurationBooleanEditor>(
        metadata = navParentKey<Destination.ManagedConfiguration>()
    ) {
        ManagedConfigurationBooleanEditorScreen(viewModel(), it.id, ::navigateUp)
    }
    myEntry<Destination.ManagedConfigurationListEditor>(
        metadata = navParentKey<Destination.ManagedConfiguration>()
    ) {
        ManagedConfigurationListEditorScreen(viewModel(), it.id, ::navigateUp)
    }
    myEntry<Destination.AppGroups> {
        AppGroupsScreen(
            viewModel(factory = container.viewModelFactory),
            { navigate(Destination.EditAppGroup) },
            ::navigateUp
        )
    }
    myEntry<Destination.EditAppGroup>(
        metadata = navParentKey<Destination.AppGroups>()
    ) {
        EditAppGroupScreen(
            viewModel(), ::navigateUp, ::choosePackage, container.chosenPackage
        )
    }

    myEntry<Destination.UserRestriction> {
        UserRestrictionScreen(
            viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate
        )
    }
    myEntry<Destination.UserRestrictionEditor>(
        metadata = navParentKey<Destination.UserRestriction>()
    ) {
        UserRestrictionEditorScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.UserRestrictionOptions>(
        metadata = navParentKey<Destination.UserRestriction>()
    ) {
        UserRestrictionOptionsScreen(it, viewModel(), ::navigateUp)
    }

    myEntry<Destination.Users> {
        UsersScreen(viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate)
    }
    myEntry<Destination.UserInfo>(
        metadata = navParentKey<Destination.Users>()
    ) {
        UserInfoScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.UsersOptions>(
        metadata = navParentKey<Destination.Users>()
    ) {
        UsersOptionsScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.UserOperation>(
        metadata = navParentKey<Destination.Users>()
    ) {
        UserOperationScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.CreateUser>(
        metadata = navParentKey<Destination.Users>()
    ) {
        CreateUserScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.ChangeUsername>(
        metadata = navParentKey<Destination.Users>()
    ) {
        ChangeUsernameScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.UserSessionMessage>(
        metadata = navParentKey<Destination.Users>()
    ) {
        UserSessionMessageScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.AffiliationId>(
        metadata = navParentKey<Destination.Users>()
    ) {
        AffiliationIdScreen(viewModel(), ::navigateUp)
    }

    myEntry<Destination.Password> {
        PasswordScreen(viewModel(factory = container.viewModelFactory), ::navigateUp, ::navigate)
    }
    myEntry<Destination.PasswordInfo>(
        metadata = navParentKey<Destination.Password>()
    ) {
        PasswordInfoScreen(
            viewModel(), ::navigateUp
        )
    }
    myEntry<Destination.ResetPasswordToken>(
        metadata = navParentKey<Destination.Password>()
    ) {
        ResetPasswordTokenScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.ResetPassword>(
        metadata = navParentKey<Destination.Password>()
    ) {
        ResetPasswordScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.RequiredPasswordComplexity>(
        metadata = navParentKey<Destination.Password>()
    ) {
        RequiredPasswordComplexityScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.KeyguardDisabledFeatures>(
        metadata = navParentKey<Destination.Password>()
    ) {
        KeyguardDisabledFeaturesScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.RequiredPasswordQuality>(
        metadata = navParentKey<Destination.Password>()
    ) {
        RequiredPasswordQualityScreen(viewModel(), ::navigateUp)
    }

    myEntry<Destination.ZenSettings>(
        metadata = navParentKey<Destination.Home>()
    ) {
        ZenSettingsScreen(viewModel(factory = container.viewModelFactory), ::navigateUp)
    }

    myEntry<Destination.TimeBlocker> {
        TimeBlockerScreen(viewModel(factory = container.viewModelFactory), ::navigate, ::navigateUp)
    }
    myEntry<Destination.TimeBlockerEdit>(
        metadata = navParentKey<Destination.TimeBlocker>()
    ) { params ->
        TimeBlockerEditScreen(
            params.ruleId, viewModel(), container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }

    myEntry<Destination.Settings> {
        SettingsScreen(viewModel(factory = container.viewModelFactory), ::navigate, ::navigateUp)
    }
    myEntry<Destination.SettingsOptions>(
        metadata = navParentKey<Destination.Settings>()
    ) {
        SettingsOptionsScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.AppearanceSettings>(
        metadata = navParentKey<Destination.Settings>()
    ) {
        AppearanceScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.ShortcutSettings>(
        metadata = navParentKey<Destination.Settings>()
    ) {
        ShortcutSettingsScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.AppLockSettings>(
        metadata = navParentKey<Destination.Settings>()
    ) {
        AppLockSettingsScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.SettingsSync>(
        metadata = navParentKey<Destination.Settings>()
    ) {
        SettingsSyncScreen(viewModel(), ::navigate, ::navigateUp)
    }
    myEntry<Destination.ApiSettings>(
        metadata = navParentKey<Destination.Settings>()
    ) {
        ApiSettings(viewModel(), ::navigateUp)
    }
    myEntry<Destination.ExportSettings>(
        metadata = navParentKey<Destination.Settings>()
    ) {
        val vm = viewModel<SettingsViewModel>()
        val qrBitmap by vm.syncQrBitmap.collectAsState()
        val exportSummary by vm.syncExportSummary.collectAsState()
        ExportSettingsScreen(qrBitmap, exportSummary, ::navigateUp)
    }
    myEntry<Destination.ImportSettings>(
        metadata = navParentKey<Destination.Settings>()
    ) {
        ImportSettingsScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.NotificationSettings>(
        metadata = navParentKey<Destination.Settings>()
    ) {
        NotificationsScreen(viewModel(), ::navigateUp)
    }
    myEntry<Destination.About> { AboutScreen(::navigateUp) }
}
