package plus.vplan.app.core.platform

import android.content.Context
import androidx.biometric.BiometricManager

class AuthenticationRepositoryImpl(context: Context) : AuthenticationRepository {
    private val biometricManager = BiometricManager.from(context)

    override fun isBiometricAuthenticationSupported(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> true
            else -> false
        }
    }

    override fun isBiometricAuthenticationEnabled(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
}
