package plus.vplan.app.ui.platform

abstract class RunBiometricAuthentication {
    abstract fun run(
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
        onCancel: () -> Unit
    )
}