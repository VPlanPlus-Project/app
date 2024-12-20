package plus.vplan.app.feature.sync.domain.usecase

import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.IndiwareRepository

class UpdateWeeksUseCase(
    private val indiwareRepository: IndiwareRepository
) {
    suspend operator fun invoke(school: School.IndiwareSchool) {
        val baseData = indiwareRepository.getBaseData(school.sp24Id, school.username, school.password)
    }
}