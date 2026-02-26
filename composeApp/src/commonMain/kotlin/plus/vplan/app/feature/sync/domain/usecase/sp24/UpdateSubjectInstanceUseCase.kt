package plus.vplan.app.feature.sync.domain.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.course.CourseRepository
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Course
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.extension.SubjectInstanceResponse
import kotlin.time.Clock
import kotlin.uuid.Uuid
import plus.vplan.lib.sp24.source.Response as Sp24Response

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

        val teachers = teacherRepository.getBySchool(school).first()
        val groups = groupRepository.getBySchool(school).first()
        val existingCourses = courseRepository.getBySchool(school)
        val existingSubjectInstances = subjectInstanceRepository.getBySchool(school)

        updateCourses(
            school = school,
            client = client,
            existingCourses = existingCourses.first(),
            teachers = teachers,
            groups = groups
        )

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
            .filter { downloadedCourse -> courseRepository.getById(downloadedCourse.sp24Alias).first() == null }

        downloadedCourses.let {
            val downloadedCoursesToDelete = existingCourses
                .filter { existingCourse ->
                    val existingAlias = existingCourse.aliases.firstOrNull { it.provider == AliasProvider.Sp24 } ?: return@filter false
                    downloadedCourseEntities.firstOrNull { it.sp24Alias.hashCode() == existingAlias.hashCode() } ?: return@filter false
                    return@filter true
                }
            LOGGER.d { "Delete ${downloadedCoursesToDelete.size} courses" }
            courseRepository.delete(downloadedCoursesToDelete)
        }

        val updatedCourses =
            downloadedCourseEntities.filter { downloadedCourse -> existingCourses.none { it.hashCode() == downloadedCourse.hashCode() } }
        LOGGER.d { "Upsert ${updatedCourses.size} courses" }
        updatedCourses
            .map { (course, sp24Alias) ->
                val teacher = course.teacher?.ifBlank { null }
                    ?.let { teachers.firstOrNull { it.name == course.teacher } }
                val existing = courseRepository.getById(sp24Alias).first()
                existing?.copy(
                    name = course.name,
                    teacher = teacher,
                    aliases = existing.aliases + sp24Alias
                )
                    ?: Course(
                        id = Uuid.random(),
                        groups = course.classes.mapNotNull { groups.firstOrNull { group -> group.name == it } }.toSet(),
                        name = course.name,
                        teacher = teacher,
                        cachedAt = Clock.System.now(),
                        aliases = setOf(sp24Alias)
                    )
            }
            .forEach { courseRepository.save(it) }
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

        val downloadedSubjectEntities = downloadedSubjectInstances.filter { subjectInstanceRepository.getById(it.sp24Alias).first() == null }

        downloadedSubjectInstances.let {
            val downloadedSubjectInstancesToDelete = existingSubjectInstances
                .filter { existingSubjectInstance ->
                    val existingAlias = existingSubjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Sp24 } ?: return@filter false
                    downloadedSubjectEntities.firstOrNull { it.sp24Alias.hashCode() == existingAlias.hashCode() } ?: return@filter false
                    return@filter true
                }

            LOGGER.d { "Delete ${downloadedSubjectInstancesToDelete.size} default lessons" }
            subjectInstanceRepository.delete(downloadedSubjectInstancesToDelete)
        }

        val updatedSubjectInstances =
            downloadedSubjectEntities.filter { downloadedSubjectInstance -> existingSubjectInstances.none { it.hashCode() == downloadedSubjectInstance.hashCode() } }
        LOGGER.d { "Upsert ${updatedSubjectInstances.size} default lessons" }
        updatedSubjectInstances
            .mapNotNull { (subjectInstance, sp24Alias) ->
                val firstGroup = subjectInstance.classes.firstNotNullOfOrNull { groupName ->
                    groupRepository.getById(identifier = Group.buildSp24Alias(sp24SchoolId.toInt(), groupName)).first() ?: return@firstNotNullOfOrNull null
                } ?: return@mapNotNull null

                val courses = courseRepository.getByGroup(firstGroup).first()

                val existing = subjectInstanceRepository.getById(sp24Alias).first()
                existing?.copy(
                    subject = subjectInstance.subject,
                    course = subjectInstance.course?.let { courses.firstOrNull { course -> course.name == subjectInstance.course } },
                    teacher = subjectInstance.teacher?.let { teachers.firstOrNull { teacher -> teacher.name == subjectInstance.teacher } },
                    groups = groups.filter { group -> group.name in subjectInstance.classes },
                    aliases = (existing.aliases + sp24Alias)
                ) ?: SubjectInstance(
                    id = Uuid.random(),
                    subject = subjectInstance.subject,
                    course = subjectInstance.course?.let { courses.firstOrNull { course -> course.name == subjectInstance.course } },
                    teacher = subjectInstance.teacher?.let { teachers.firstOrNull { teacher -> teacher.name == subjectInstance.teacher } },
                    groups = groups.filter { group -> group.name in subjectInstance.classes },
                    cachedAt = Clock.System.now(),
                    aliases = setOf(sp24Alias)
                )
            }.forEach { subjectInstanceRepository.save(it) }
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