package plus.vplan.app.ui.platform

interface RunBiometricAuthentication {
    fun run(
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
        onCancel: () -> Unit
    )
}