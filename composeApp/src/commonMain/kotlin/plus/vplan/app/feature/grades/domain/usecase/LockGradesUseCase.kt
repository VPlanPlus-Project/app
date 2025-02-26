package plus.vplan.app.feature.grades.domain.usecase

import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class LockGradesUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke() {
        keyValueRepository.set(Keys.GRADES_LOCKED, "true")
    }
}