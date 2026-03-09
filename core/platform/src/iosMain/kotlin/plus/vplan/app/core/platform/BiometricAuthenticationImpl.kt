@file:OptIn(ExperimentalForeignApi::class)

package plus.vplan.app.core.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorUserCancel
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

class BiometricAuthenticationImpl : BiometricAuthentication {
    override fun run(
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
        onCancel: () -> Unit
    ) {
        val context = LAContext()
        context.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = "$title-$subtitle"
        ) { success, authError ->
            if (success) onSuccess()
            else when (authError?.code) {
                LAErrorUserCancel -> onCancel()
                else -> onError()
            }
        }
    }
}
