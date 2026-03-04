package plus.vplan.app.feature.settings.page.security.domain.usecase

import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.feature.settings.page.security.ui.GradeProtectLevel

class SetGradeProtectionLevelUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(state: GradeProtectLevel) {
        keyValueRepository.set(Keys.GRADE_PROTECTION_LEVEL, state.name)
        keyValueRepository.set(Keys.GRADES_LOCKED, "false")
    }
}