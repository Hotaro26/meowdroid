package com.hotaro.meowdroid.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.hotaro.meowdroid.R
import com.hotaro.meowdroid.utils.popToast

class ProvisioningPolicyComplianceActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_OK)
        popToast(R.string.app_name)
        finish()
    }
}
