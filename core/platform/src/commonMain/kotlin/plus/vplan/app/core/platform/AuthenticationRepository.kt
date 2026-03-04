package plus.vplan.app.core.platform

interface AuthenticationRepository {
    fun isBiometricAuthenticationSupported(): Boolean
    fun isBiometricAuthenticationEnabled(): Boolean
}
