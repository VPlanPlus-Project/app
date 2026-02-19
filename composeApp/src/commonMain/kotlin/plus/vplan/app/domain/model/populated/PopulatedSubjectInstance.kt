package plus.vplan.app.domain.model.populated

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.core.model.Course
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.TeacherRepository

@Immutable
data class PopulatedSubjectInstance(
    val subjectInstance: SubjectInstance,
    val course: Course?,
    val teacher: Teacher?,
    val groups: List<Group>
)

class SubjectInstancePopulator: KoinComponent {
    private val courseRepository by inject<CourseRepository>()
    private val teacherRepository by inject<TeacherRepository>()
    private val groupRepository by inject<GroupRepository>()

    fun populateMultiple(subjectInstances: List<SubjectInstance>, context: PopulationContext?): Flow<List<PopulatedSubjectInstance>> {
        val courses =
            when (context) {
                is PopulationContext.School -> courseRepository.getBySchool(context.school.id)
                is PopulationContext.Profile if context.profile is Profile.StudentProfile -> courseRepository.getByGroup(context.profile.group.id)
                else -> courseRepository.getAll()
            }

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

        return combine(courses, teachers, groups) { courses, teachers, groups ->
            subjectInstances.map { subjectInstance ->
                PopulatedSubjectInstance(
                    subjectInstance = subjectInstance,
                    course = courses.firstOrNull { it.id == subjectInstance.courseId },
                    teacher = teachers.firstOrNull { it.id == subjectInstance.teacherId },
                    groups = groups.filter { it.id in subjectInstance.groupIds }
                )
            }
        }
    }

    fun populateSingle(subjectInstance: SubjectInstance): Flow<PopulatedSubjectInstance> {
        val teacher = subjectInstance.teacherId?.let { teacherId -> teacherRepository.getByLocalId(teacherId) } ?: flowOf(null)
        val course = subjectInstance.courseId?.let { courseId -> courseRepository.getByLocalId(courseId) } ?: flowOf(null)
        val groups =
            if (subjectInstance.groupIds.isEmpty()) flowOf(emptyList())
            else combine(subjectInstance.groupIds.map { groupId -> groupRepository.getByLocalId(groupId) }) { it.filterNotNull() }

        return combine(teacher, course, groups) { teacher, course, groups ->
            PopulatedSubjectInstance(
                subjectInstance = subjectInstance,
                course = course,
                teacher = teacher,
                groups = groups
            )
        }
    }
}