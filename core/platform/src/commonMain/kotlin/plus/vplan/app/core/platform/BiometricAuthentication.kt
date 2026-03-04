package plus.vplan.app.core.platform

interface BiometricAuthentication {
    fun run(
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
        onCancel: () -> Unit
    )
}
