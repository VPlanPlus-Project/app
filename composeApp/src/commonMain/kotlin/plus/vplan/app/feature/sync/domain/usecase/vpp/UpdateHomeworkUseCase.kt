package plus.vplan.app.feature.sync.domain.usecase.vpp

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.ProfileRepository

class UpdateHomeworkUseCase(
    private val profileRepository: ProfileRepository,
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke() {
        val ids = mutableSetOf<Int>()
        profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>().forEach { studentProfile ->
            ids.addAll(
                (homeworkRepository.download(
                    schoolApiAccess = studentProfile.getVppIdItem()?.buildSchoolApiAccess(studentProfile.getSchoolItem().id) ?: studentProfile.getSchool().getFirstValue()!!.getSchoolApiAccess(),
                    groupId = studentProfile.group,
                    defaultLessonIds = studentProfile.defaultLessons.map { it.key },
                ) as? Response.Success)?.data.orEmpty()
            )
        }

        homeworkRepository.deleteById(homeworkRepository.getAll().first().filterIsInstance<CacheState.Done<Homework.CloudHomework>>().map { it.data }.filter { it.id !in ids }.map { it.id })
    }
}