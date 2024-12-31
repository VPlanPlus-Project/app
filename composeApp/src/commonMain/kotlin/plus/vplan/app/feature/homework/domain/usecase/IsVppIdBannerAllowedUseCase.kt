package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class IsVppIdBannerAllowedUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return keyValueRepository.get(Keys.SHOW_HOMEWORK_VPP_ID_BANNER).map { it?.toBoolean() ?: true }
    }
}