package plus.vplan.app.feature.settings.page.security.domain.usecase

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import plus.vplan.app.domain.repository.PlatformAuthenticationRepository

class GetBiometricDeviceStateUseCase(
    private val platformAuthenticationRepository: PlatformAuthenticationRepository
) {
    operator fun invoke() = flow {
        while (true) {
            if (!platformAuthenticationRepository.isBiometricAuthenticationSupported()) emit(BiometricDeviceState.NotAvailable)
            else if (!platformAuthenticationRepository.isBiometricAuthenticationEnabled()) emit(BiometricDeviceState.NotEnrolled)
            else emit(BiometricDeviceState.Ready)
            kotlinx.coroutines.delay(500)
        }
    }.distinctUntilChanged()
}

enum class BiometricDeviceState {
    Ready, NotEnrolled, NotAvailable
}