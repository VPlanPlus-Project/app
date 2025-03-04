package plus.vplan.app.ui.platform

class RunBiometricAuthenticationImpl : RunBiometricAuthentication {
    override fun run(
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
        onCancel: () -> Unit
    ) {
        TODO("This is not yet supported on iOS")
    }
}