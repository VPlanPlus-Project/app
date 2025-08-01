package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.IndiwareClient

private val LOGGER = Logger.withTag("UpdateSubjectInstanceUseCase")

class UpdateSubjectInstanceUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val teacherRepository: TeacherRepository,
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(school: School.Sp24School, providedClient: IndiwareClient?): Response.Error? {
        val client = providedClient ?: indiwareRepository.getSp24Client(
            Authentication(school.sp24Id, school.username, school.password),
            withCache = true
        )

        val teachers = teacherRepository.getBySchool(schoolId = school.id).first()
        val existingCourses = courseRepository.getBySchool(schoolId = school.id, false)
        val existingSubjectInstances = subjectInstanceRepository.getBySchool(schoolId = school.id, false)

        updateCourses(
            school = school,
            client = client,
            existingCourses = existingCourses.first(),
            teachers = teachers
        )

        updateSubjectInstances(
            client = client,
            existingSubjectInstances = existingSubjectInstances.first(),
        )

        return null
    }

    private suspend fun updateCourses(
        school: School.Sp24School,
        client: IndiwareClient,
        existingCourses: List<Course>,
        teachers: List<Teacher>
    ): Boolean {
        val downloadedCourses =
            (client.subjectInstances.getSubjectInstances() as? plus.vplan.lib.sp24.source.Response.Success)?.data?.courses ?: return false

        val downloadedCourseEntities = downloadedCourses
            .map { course -> Course.fromIndiware(school.sp24Id, course.name, teacher = teachers.firstOrNull { it.name == course.teacher }) }
            .mapNotNull { courseRepository.getByIndiwareId(it).getFirstValueOld() }

        downloadedCourses.let {
            val existingCourseIds = existingCourses.map { it.id }
            val downloadedCoursesToDelete = existingCourseIds.filter { existingCourseId -> downloadedCourseEntities.none { it.id == existingCourseId } }
            LOGGER.d { "Delete ${downloadedCoursesToDelete.size} courses" }
            courseRepository.deleteById(downloadedCoursesToDelete)
        }

        val updatedCourses = downloadedCourseEntities.filter { downloadedCourse -> existingCourses.none { it.hashCode() == downloadedCourse.hashCode() } }
        LOGGER.d { "Upsert ${updatedCourses.size} courses" }
        courseRepository.upsert(updatedCourses)
        return true
    }

    private suspend fun updateSubjectInstances(
        client: IndiwareClient,
        existingSubjectInstances: List<SubjectInstance>,
    ): Boolean {
        val sp24SchoolId = client.authentication.indiwareSchoolId
        val downloadedSubjectInstances =
            (client.subjectInstances.getSubjectInstances() as? plus.vplan.lib.sp24.source.Response.Success) ?: return false

        val downloadedSubjectEntities = downloadedSubjectInstances.data.subjectInstances
            .mapNotNull { subjectInstanceRepository.lookupBySp24Id("sp24.$sp24SchoolId.${it.id}").getFirstValueOld() }

        downloadedSubjectInstances.let {
            val existingSubjectInstanceIds = existingSubjectInstances.map { it.id }
            val downloadedSubjectInstancesToDelete = existingSubjectInstanceIds.filter { existingSubjectInstanceId -> downloadedSubjectEntities.none { it.id == existingSubjectInstanceId } }
            LOGGER.d { "Delete ${downloadedSubjectInstancesToDelete.size} default lessons" }
            subjectInstanceRepository.deleteById(downloadedSubjectInstancesToDelete)
        }

        val updatedSubjectInstances = downloadedSubjectEntities.filter { downloadedSubjectInstance -> existingSubjectInstances.none { it.hashCode() == downloadedSubjectInstance.hashCode() } }
        LOGGER.d { "Upsert ${updatedSubjectInstances.size} default lessons" }
        subjectInstanceRepository.upsert(updatedSubjectInstances)
        return true
    }
}