package plus.vplan.app.domain.model.populated

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.core.model.Course
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.TeacherRepository

data class PopulatedCourse(
    val course: Course,
    val teacher: Teacher?,
    val groups: List<Group>
)

class CoursePopulator: KoinComponent {
    private val teacherRepository by inject<TeacherRepository>()
    private val groupRepository by inject<GroupRepository>()

    fun populateMultiple(courses: List<Course>, context: PopulationContext?): Flow<List<PopulatedCourse>> {
        val teachers =
            when (context) {
                is PopulationContext.School -> teacherRepository.getBySchool(context.school.id)
                is PopulationContext.Profile -> teacherRepository.getBySchool(context.profile.school.id)
                else -> teacherRepository.getAll()
            }

        val groups =
            when (context) {
                is PopulationContext.School -> groupRepository.getBySchool(context.school.id)
                is PopulationContext.Profile -> groupRepository.getBySchool(context.profile.school.id)
                else -> groupRepository.getAll()
            }

        return combine(teachers, groups) { teachers, groups ->
            courses.map { course ->
                PopulatedCourse(
                    course = course,
                    teacher = teachers.find { it.id == course.teacherId },
                    groups = groups.filter { course.groupIds.contains(it.id) }
                )
            }
        }
    }

    fun populateSingle(course: Course): Flow<PopulatedCourse> {
        val teacher = course.teacherId?.let { teacherId -> teacherRepository.getByLocalId(teacherId) } ?: flowOf(null)
        val groups = combine(course.groupIds.map { groupId -> groupRepository.getByLocalId(groupId) }) { it.filterNotNull() }

        return combine(teacher, groups) { teacher, groups ->
            PopulatedCourse(
                course = course,
                teacher = teacher,
                groups = groups
            )
        }
    }
}