package plus.vplan.app.core.common.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys

class IsVppIdBannerAllowedUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return keyValueRepository.get(Keys.SHOW_HOMEWORK_VPP_ID_BANNER).map { it?.toBoolean() ?: true }
    }
}