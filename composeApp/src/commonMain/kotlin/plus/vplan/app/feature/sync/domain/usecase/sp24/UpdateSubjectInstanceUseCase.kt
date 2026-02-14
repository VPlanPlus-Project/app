package plus.vplan.app.feature.sync.domain.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.Group
import plus.vplan.app.core.model.School
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.CourseDbDto
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.SubjectInstanceDbDto
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Response as Sp24Response
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.extension.SubjectInstanceResponse

private val LOGGER = Logger.withTag("UpdateSubjectInstanceUseCase")

class UpdateSubjectInstanceUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(
        school: School.AppSchool,
        providedClient: Stundenplan24Client?
    ): Response.Error? {
        val client = providedClient ?: stundenplan24Repository.getSp24Client(
            Authentication(school.sp24Id, school.username, school.password),
            withCache = true
        )

        val teachers = teacherRepository.getBySchool(schoolId = school.id).first()
        val groups = groupRepository.getBySchool(school.id).first()
        val existingCourses = courseRepository.getBySchool(school.id)
        val existingSubjectInstances = subjectInstanceRepository.getBySchool(schoolId = school.id)

        updateCourses(
            school = school,
            client = client,
            existingCourses = existingCourses.first(),
            teachers = teachers,
            groups = groups
        )

        courseRepository.getBySchool(school.id).first()

        updateSubjectInstances(
            client = client,
            existingSubjectInstances = existingSubjectInstances.first(),
            teachers = teachers,
            groups = groups
        )

        return null
    }

    private suspend fun updateCourses(
        school: School.AppSchool,
        client: Stundenplan24Client,
        existingCourses: List<Course>,
        teachers: List<Teacher>,
        groups: List<Group>
    ): Boolean {
        val downloadedCourses = ((client.subjectInstances.getSubjectInstances() as? Sp24Response.Success)?.data?.courses ?: return false)
            .map { DownloadedCourse(it, Course.buildSp24Alias(school.sp24Id, it.name, it.classes.sorted().toSet(), it.teacher)) }

        val downloadedCourseEntities = downloadedCourses
            .filter { downloadedCourse -> courseRepository.resolveAliasToLocalId(downloadedCourse.sp24Alias) == null }

        downloadedCourses.let {
            val downloadedCoursesToDelete = existingCourses
                .filter { existingCourse ->
                    val existingAlias = existingCourse.aliases.firstOrNull { it.provider == AliasProvider.Sp24 } ?: return@filter false
                    downloadedCourseEntities.firstOrNull { it.sp24Alias.hashCode() == existingAlias.hashCode() } ?: return@filter false
                    return@filter true
                }
            LOGGER.d { "Delete ${downloadedCoursesToDelete.size} courses" }
            courseRepository.deleteById(downloadedCoursesToDelete.map { it.id })
        }

        val updatedCourses =
            downloadedCourseEntities.filter { downloadedCourse -> existingCourses.none { it.hashCode() == downloadedCourse.hashCode() } }
        LOGGER.d { "Upsert ${updatedCourses.size} courses" }
        updatedCourses
            .map { (course, sp24Alias) ->
                val teacher = course.teacher?.ifBlank { null }
                    ?.let { teachers.firstOrNull { it.name == course.teacher } }
                CourseDbDto(
                    name = course.name,
                    groups = course.classes.mapNotNull { groups.firstOrNull { group -> group.name == it } }
                        .map { it.id },
                    teacher = teacher?.id,
                    aliases = listOf(sp24Alias)
                )
            }
            .forEach { courseRepository.upsert(it) }
        return true
    }

    private suspend fun updateSubjectInstances(
        client: Stundenplan24Client,
        existingSubjectInstances: List<SubjectInstance>,
        teachers: List<Teacher>,
        groups: List<Group>
    ): Boolean {
        val sp24SchoolId = client.authentication.sp24SchoolId
        val downloadedSubjectInstances = ((client.subjectInstances.getSubjectInstances() as? Sp24Response.Success) ?: return false).let {
            it.data.subjectInstances.map { si -> DownloadedSubjectInstance(si, SubjectInstance.buildSp24Alias(sp24SchoolId.toInt(), si.id)) }
        }

        val downloadedSubjectEntities = downloadedSubjectInstances.filter { subjectInstanceRepository.resolveAliasToLocalId(it.sp24Alias) == null }

        downloadedSubjectInstances.let {
            val downloadedSubjectInstancesToDelete = existingSubjectInstances
                .filter { existingSubjectInstance ->
                    val existingAlias = existingSubjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Sp24 } ?: return@filter false
                    downloadedSubjectEntities.firstOrNull { it.sp24Alias.hashCode() == existingAlias.hashCode() } ?: return@filter false
                    return@filter true
                }
            LOGGER.d { "Delete ${downloadedSubjectInstancesToDelete.size} default lessons" }
            subjectInstanceRepository.deleteById(downloadedSubjectInstancesToDelete.map { it.id })
        }

        val updatedSubjectInstances =
            downloadedSubjectEntities.filter { downloadedSubjectInstance -> existingSubjectInstances.none { it.hashCode() == downloadedSubjectInstance.hashCode() } }
        LOGGER.d { "Upsert ${updatedSubjectInstances.size} default lessons" }
        updatedSubjectInstances
            .mapNotNull { (subjectInstance, sp24Alias) ->
                val firstGroup = subjectInstance.classes.firstNotNullOfOrNull { groupName ->
                    val id = groupRepository.resolveAliasToLocalId(Group.buildSp24Alias(sp24SchoolId.toInt(), groupName)) ?: return@firstNotNullOfOrNull null
                    groupRepository.getByLocalId(id).first()
                } ?: return@mapNotNull null

                val courses = courseRepository.getByGroup(firstGroup.id).first()
                SubjectInstanceDbDto(
                    subject = subjectInstance.subject,
                    course = subjectInstance.course?.let { courses.firstOrNull { course -> course.name == subjectInstance.course } }?.id,
                    teacher = subjectInstance.teacher?.let { teachers.firstOrNull { teacher -> teacher.name == subjectInstance.teacher }?.id },
                    groups = groups.filter { group -> group.name in subjectInstance.classes }
                        .map { it.id },
                    aliases = listOf(sp24Alias)
                )
            }.forEach { subjectInstanceRepository.upsert(it) }
        return true
    }
}

private data class DownloadedCourse(
    val course: SubjectInstanceResponse.Course,
    val sp24Alias: Alias
)

private data class DownloadedSubjectInstance(
    val subjectInstance: SubjectInstanceResponse.SubjectInstance,
    val sp24Alias: Alias
)