package plus.vplan.app.core.platform

class BiometricAuthenticationImpl : BiometricAuthentication {
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
