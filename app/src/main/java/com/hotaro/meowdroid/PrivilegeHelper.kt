package com.hotaro.meowdroid

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.hotaro.meowdroid.utils.DhizukuError
import com.hotaro.meowdroid.utils.DhizukuException
import com.hotaro.meowdroid.utils.binderWrapperDevicePolicyManager
import com.rosan.dhizuku.api.Dhizuku
import kotlinx.coroutines.flow.MutableStateFlow

class PrivilegeHelper(
    val context: Context, var dhizuku: Boolean, val dhizukuError: MutableStateFlow<DhizukuError?>
) {
    val myDpm = context.getSystemService(DevicePolicyManager::class.java)!!
    val myDar = ComponentName(context, Receiver::class.java)

    val dpm: DevicePolicyManager
        get() {
            return if (dhizuku) getDhizukuDpm() else myDpm
        }

    val dar: ComponentName
        get() {
            return if (dhizuku) Dhizuku.getOwnerComponent() else myDar
        }

    class SafeDpmCallScope(val dpm: DevicePolicyManager, val dar: ComponentName)

    fun safeDpmCall(block: SafeDpmCallScope.() -> Unit) {
        try {
            SafeDpmCallScope(dpm, dar).block()
        } catch (e: DhizukuException) {
            dhizukuError.value = e.reason
        }
    }

    private fun getDhizukuDpm(): DevicePolicyManager {
        try {
            if (!Dhizuku.init(context)) throw DhizukuException(DhizukuError.Init)
            if (!Dhizuku.isPermissionGranted()) throw DhizukuException(DhizukuError.Permission)
            return binderWrapperDevicePolicyManager(context)
        } catch(e: Exception) {
            if (e !is DhizukuException) {
                throw DhizukuException(DhizukuError.Binder, e)
            }
            throw e
        }
    }
}
