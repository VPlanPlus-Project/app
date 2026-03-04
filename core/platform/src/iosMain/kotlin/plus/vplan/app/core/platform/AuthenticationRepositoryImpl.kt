package plus.vplan.app.core.platform

class AuthenticationRepositoryImpl : AuthenticationRepository {
    override fun isBiometricAuthenticationSupported(): Boolean = false
    override fun isBiometricAuthenticationEnabled(): Boolean = false
}
