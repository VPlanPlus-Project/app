package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class UpdateProfileHomeworkIndexUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(profile: Profile) {
        homeworkRepository.dropIndexForProfile(profile.id)
        homeworkRepository.getAll()
            .first { list -> list.all { it is CacheStateOld.Done || it is CacheStateOld.NotExisting } }
            .mapNotNull{ (it as? CacheStateOld.Done<Homework>)?.data }
            .filter { homework ->
                (homework.creator is AppEntity.Profile && homework.creator.id == profile.id) ||
                        (homework.creator is AppEntity.VppId && homework.creator.id == (profile as? Profile.StudentProfile)?.vppIdId) ||
                        (homework.group?.getFirstValue()?.id == (profile as? Profile.StudentProfile)?.groupId && profile is Profile.StudentProfile) ||
                        (homework.subjectInstance?.getFirstValueOld()?.id in (profile as? Profile.StudentProfile)?.subjectInstanceConfiguration.orEmpty().filterValues { it }.keys && profile is Profile.StudentProfile)
            }
            .let { relevantHomework ->
                homeworkRepository.createCacheForProfile(profile.id, relevantHomework.map { it.id })
            }
    }
}