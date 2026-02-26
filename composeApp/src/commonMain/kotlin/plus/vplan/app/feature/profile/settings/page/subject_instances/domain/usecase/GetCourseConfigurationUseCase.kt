package plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase

import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.model.Course
import plus.vplan.app.core.model.Profile
import plus.vplan.app.utils.filterKeysNotNull
import plus.vplan.app.utils.sortedBySuspending

class GetCourseConfigurationUseCase: KoinComponent {
    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()

    suspend operator fun invoke(profile: Profile.StudentProfile): Map<Course, Boolean?> {
        val subjectInstances = subjectInstanceRepository.getByGroup(profile.group).first()
        return subjectInstances
            .filter { subjectInstance -> profile.subjectInstanceConfiguration[subjectInstance.id] != false }
            .groupBy { it.course }
            .mapValues { (_, subjectInstances) ->
                val selections =
                    subjectInstances.map { profile.subjectInstanceConfiguration[it.id] == true }
                if (selections.all { it }) true else if (selections.any { it }) null else false
            }
            .filterKeysNotNull()
            .sortedBySuspending { (course, _) ->
                course.name + course.teacher?.name
            }
            .associate { it.key to it.value }
    }
}