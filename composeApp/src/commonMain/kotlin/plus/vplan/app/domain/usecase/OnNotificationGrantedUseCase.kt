package plus.vplan.app.domain.usecase

import plus.vplan.app.domain.repository.PlatformNotificationRepository

class OnNotificationGrantedUseCase(
    private val platformNotificationRepository: PlatformNotificationRepository
) {
    suspend operator fun invoke() {
        platformNotificationRepository.initialize()
    }
}