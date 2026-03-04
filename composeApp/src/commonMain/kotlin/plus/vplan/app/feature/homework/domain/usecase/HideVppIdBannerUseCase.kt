package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys

class HideVppIdBannerUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke() {
        keyValueRepository.set(Keys.SHOW_HOMEWORK_VPP_ID_BANNER, "false")
    }
}