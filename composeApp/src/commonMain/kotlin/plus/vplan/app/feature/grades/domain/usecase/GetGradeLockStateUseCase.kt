package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.feature.settings.page.security.domain.usecase.GetGradeProtectionLevelUseCase
import plus.vplan.app.feature.settings.page.security.ui.GradeProtectLevel

class GetGradeLockStateUseCase(
    private val getGradeProtectionLevelUseCase: GetGradeProtectionLevelUseCase,
    private val keyValueRepository: KeyValueRepository
) {
     operator fun invoke() = combine(
         keyValueRepository.get(Keys.GRADES_LOCKED).map { it?.toBoolean() ?: false },
         getGradeProtectionLevelUseCase()
     ) { locked, gradeProtectionLevel ->
         if (gradeProtectionLevel == GradeProtectLevel.None) GradeLockState.NotConfigured
         else if (locked) GradeLockState.Locked
         else GradeLockState.Unlocked
     }
}

enum class GradeLockState(val canAccess: Boolean) {
    Locked(false), Unlocked(true), NotConfigured(true)
}