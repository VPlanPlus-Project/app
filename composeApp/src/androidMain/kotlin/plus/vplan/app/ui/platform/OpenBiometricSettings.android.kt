package plus.vplan.app.ui.platform

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG

class OpenBiometricSettingsImpl(
    private val context: Context
) : OpenBiometricSettings() {
    override fun run() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(enrollIntent)
        }
    }
}