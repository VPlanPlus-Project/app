package plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase

import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Course
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.utils.filterKeysNotNull
import plus.vplan.app.utils.sortedBySuspending

class GetCourseConfigurationUseCase: KoinComponent {
    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()
    private val teacherRepository by inject<TeacherRepository>()
    private val courseRepository by inject<CourseRepository>()

    suspend operator fun invoke(profile: Profile.StudentProfile): Map<Course, Boolean?> {
        val subjectInstances = subjectInstanceRepository.getByGroup(profile.group.id).first()
        return subjectInstances
            .filter { subjectInstance -> profile.subjectInstanceConfiguration[subjectInstance.id] != false }
            .groupBy { it.courseId }
            .mapKeys { it.key?.let { courseId -> courseRepository.getByLocalId(courseId) }?.first() }
            .mapValues { (_, subjectInstances) ->
                val selections =
                    subjectInstances.map { profile.subjectInstanceConfiguration[it.id] == true }
                if (selections.all { it }) true else if (selections.any { it }) null else false
            }
            .filterKeysNotNull()
            .sortedBySuspending { (course, enabled) ->
                val teacher = course.teacherId?.let { teacherRepository.getByLocalId(it).first() }
                course.name + teacher?.name
            }
            .associate { it.key to it.value }
    }
}