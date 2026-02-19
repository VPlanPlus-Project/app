package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.populated.HomeworkPopulator
import plus.vplan.app.domain.model.populated.PopulatedHomework
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.domain.repository.HomeworkRepository

class UpdateProfileHomeworkIndexUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val homeworkPopulator: HomeworkPopulator
) {
    suspend operator fun invoke(profile: Profile) {
        homeworkRepository.dropIndexForProfile(profile.id)
        homeworkRepository.getAll()
            .first()
            .let { homeworkPopulator.populateMultiple(it, PopulationContext.Profile(profile)).first() }
            .filter { homework ->
                val homework = homework
                (homework is PopulatedHomework.LocalHomework && homework.createdByProfile.id == profile.id) ||
                        (homework is PopulatedHomework.CloudHomework && homework.createdByUser.id == (profile as? Profile.StudentProfile)?.vppId?.id) ||
                        (homework.group?.id == (profile as? Profile.StudentProfile)?.group?.id && profile is Profile.StudentProfile) ||
                        (homework.subjectInstance?.id in (profile as? Profile.StudentProfile)?.subjectInstanceConfiguration.orEmpty().filterValues { it }.keys && profile is Profile.StudentProfile)
            }
            .let { relevantHomework ->
                homeworkRepository.createCacheForProfile(profile.id, relevantHomework.map { it.homework.id })
            }
    }
}
