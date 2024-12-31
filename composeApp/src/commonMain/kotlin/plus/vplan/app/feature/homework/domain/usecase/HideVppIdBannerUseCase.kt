package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class HideVppIdBannerUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke() {
        keyValueRepository.set(Keys.SHOW_HOMEWORK_VPP_ID_BANNER, "false")
    }
}