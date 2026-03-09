@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package plus.vplan.app.core.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.Foundation.NSError
import platform.LocalAuthentication.LABiometryTypeNone
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

class AuthenticationRepositoryImpl : AuthenticationRepository {

    override fun isBiometricAuthenticationSupported(): Boolean {
        val context = LAContext()
        // We check for hardware availability regardless of whether it's currently enrolled
        // Even if not enrolled, canEvaluatePolicy returns false but populates biometryType
        context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
        return context.biometryType != LABiometryTypeNone
    }

    override fun isBiometricAuthenticationEnabled(): Boolean = memScoped {
        val context = LAContext()
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()

        val canEvaluate = context.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            errorPtr.ptr
        )

        // If canEvaluate is true, it is both supported AND enrolled (enabled)
        if (canEvaluate) return true

        // If it failed because it's not enrolled, the hardware is there but not "enabled"
        // If it failed for other reasons (like .biometryNotAvailable), it's not supported
        return false
    }
}