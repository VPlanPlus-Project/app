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

    suspend operator fun invoke(profile: Profile.StudentProfile): List<Pair<Course, Boolean?>> {
        val subjectInstances = subjectInstanceRepository.getByGroup(profile.group).first()
        return subjectInstances
            .groupBy { it.course }
            .filterKeysNotNull()
            .mapValues { (_, subjectInstances) ->
                val selections =
                    subjectInstances.map { profile.subjectInstanceConfiguration[it.id] == true }
                if (selections.all { it }) true else if (selections.any { it }) null else false
            }
            .toList()
            .sortedBySuspending { (course, _) ->
                course.name + course.teacher?.name
            }
    }
}