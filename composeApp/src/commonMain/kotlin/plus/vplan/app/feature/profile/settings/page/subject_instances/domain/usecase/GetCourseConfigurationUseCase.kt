package plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase

import kotlinx.coroutines.runBlocking
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.Profile

class GetCourseConfigurationUseCase {
    suspend operator fun invoke(profile: Profile.StudentProfile): Map<Course, Boolean?> {
        return profile.getSubjectInstances()
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