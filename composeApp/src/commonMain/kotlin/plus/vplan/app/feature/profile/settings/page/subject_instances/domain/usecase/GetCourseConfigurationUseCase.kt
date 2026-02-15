package plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.domain.model.Course
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.SubjectInstanceRepository

class GetCourseConfigurationUseCase: KoinComponent {
    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()


    suspend operator fun invoke(profile: Profile.StudentProfile): Map<Course, Boolean?> {
        val subjectInstances = subjectInstanceRepository.getByGroup(profile.group.id).first()
        return subjectInstances
            .filter { subjectInstance -> profile.subjectInstanceConfiguration[subjectInstance.id] != false }
            .groupBy { it.getCourseItem() }
            .map { (course, subjectInstances) ->
                val selections = subjectInstances.map { profile.subjectInstanceConfiguration[it.id] == true }
                course to if (selections.all { it }) true else if (selections.any { it }) null else false
            }
            .filter { it.first != null }
            .sortedBy { runBlocking { it.first!!.name + it.first!!.teacher?.getFirstValue()?.name } }
            .associate { it.first!! to it.second }
    }
}