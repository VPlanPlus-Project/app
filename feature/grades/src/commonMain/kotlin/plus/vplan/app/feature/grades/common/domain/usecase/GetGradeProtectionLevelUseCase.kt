package plus.vplan.app.feature.grades.common.domain.usecase

import kotlinx.coroutines.flow.map
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.feature.grades.common.domain.model.GradeProtectLevel

class GetGradeProtectionLevelUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    operator fun invoke() = keyValueRepository.get(Keys.GRADE_PROTECTION_LEVEL).map {
        if (it == null) return@map GradeProtectLevel.None
        try {
            return@map GradeProtectLevel.valueOf(it)
        } catch (e: IllegalArgumentException) {
            return@map GradeProtectLevel.None
        }
    }
}