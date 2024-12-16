package plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

class SetUpSchoolData(
    private val onboardingRepository: OnboardingRepository,
    private val indiwareRepository: IndiwareRepository,
    private val schoolRepository: SchoolRepository
) {
    suspend operator fun invoke(): Boolean {
        val sp24Id = onboardingRepository.getSp24OnboardingSchool().first()?.sp24Id?.toString() ?: return false
        val username = onboardingRepository.getSp24Credentials()?.username ?: return false
        val password = onboardingRepository.getSp24Credentials()?.password ?: return false
        val result = indiwareRepository.getBaseData(
            sp24Id = sp24Id,
            username = username,
            password = password
        )
        if (result !is Response.Success) return false

        val schoolId = schoolRepository.getIdFromSp24Id(sp24Id.toInt())
        if (schoolId !is Response.Success) return false
        val school = schoolRepository.getWithCachingById(schoolId.data).let {
            if (it is Response.Success) it.data.first() else return false
        }

        result.data.classes.forEach { classData ->

        }

        return true
    }
}