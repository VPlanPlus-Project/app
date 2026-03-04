package plus.vplan.app.domain.usecase

import plus.vplan.app.core.platform.NotificationRepository

class OnNotificationGrantedUseCase(
    private val platformNotificationRepository: NotificationRepository
) {
    suspend operator fun invoke() {
        platformNotificationRepository.initialize()
    }
}