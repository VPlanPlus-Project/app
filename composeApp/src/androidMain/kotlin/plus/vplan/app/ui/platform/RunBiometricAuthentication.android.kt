package plus.vplan.app.ui.platform

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.koin.core.context.GlobalContext
import plus.vplan.app.domain.repository.ActivityProvider

class RunBiometricAuthenticationImpl : RunBiometricAuthentication {

    override fun run(title: String, subtitle: String, negativeButtonText: String, onSuccess: () -> Unit, onError: () -> Unit, onCancel: () -> Unit) {
        val activityProvider = GlobalContext.get().get<ActivityProvider>()
        val fragmentActivity = activityProvider.currentActivity as? FragmentActivity ?: return

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        val executor = ContextCompat.getMainExecutor(fragmentActivity)
        val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    onError()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onCancel()
                }
            })

        biometricPrompt.authenticate(promptInfo)

    }
}