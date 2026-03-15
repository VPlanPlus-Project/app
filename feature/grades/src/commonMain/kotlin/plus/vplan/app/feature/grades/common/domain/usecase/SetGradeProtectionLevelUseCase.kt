package plus.vplan.app.feature.grades.common.domain.usecase

import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.feature.grades.common.domain.model.GradeProtectLevel

class SetGradeProtectionLevelUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(state: GradeProtectLevel) {
        keyValueRepository.set(Keys.GRADE_PROTECTION_LEVEL, state.name)
        keyValueRepository.set(Keys.GRADES_LOCKED, "false")
    }
}