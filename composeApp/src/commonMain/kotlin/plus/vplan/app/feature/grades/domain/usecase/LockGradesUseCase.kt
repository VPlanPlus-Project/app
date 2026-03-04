package plus.vplan.app.feature.grades.domain.usecase

import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys

class LockGradesUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke() {
        keyValueRepository.set(Keys.GRADES_LOCKED, "true")
    }
}