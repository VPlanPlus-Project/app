package plus.vplan.app.domain.repository

interface PlatformAuthenticationRepository {
    fun isBiometricAuthenticationSupported(): Boolean
    fun isBiometricAuthenticationEnabled(): Boolean
}