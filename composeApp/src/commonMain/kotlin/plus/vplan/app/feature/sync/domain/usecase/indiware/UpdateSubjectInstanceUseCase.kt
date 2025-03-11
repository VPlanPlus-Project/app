package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.IndiwareBaseData
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.utils.latest

private val LOGGER = Logger.withTag("UpdateSubjectInstanceUseCase")

class UpdateSubjectInstanceUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val teacherRepository: TeacherRepository,
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(school: School.IndiwareSchool): Response.Error? {
        val baseData = indiwareRepository.getBaseData(school.sp24Id, school.username, school.password)
        if (baseData is Response.Error) return baseData
        if (baseData !is Response.Success) throw IllegalStateException("baseData is not successful: $baseData")

        val teachers = teacherRepository.getBySchool(schoolId = school.id).first()
        val existingCourses = courseRepository.getBySchool(schoolId = school.id, false)
        val existingSubjectInstances = subjectInstanceRepository.getBySchool(schoolId = school.id, false)

        updateCourses(
            school = school,
            baseData = baseData.data,
            existingCourses = existingCourses.latest(),
            teachers = teachers
        )

        updateSubjectInstances(
            baseData = baseData.data,
            existingSubjectInstances = existingSubjectInstances.latest(),
        )

        return null
    }

    private suspend fun updateCourses(
        school: School.IndiwareSchool,
        baseData: IndiwareBaseData,
        existingCourses: List<Course>,
        teachers: List<Teacher>
    ) {
        val downloadedCourses = baseData.classes
            .flatMap { baseDataClass -> baseDataClass.subjectInstances.mapNotNull { it.course }.map { Course.fromIndiware(school.sp24Id, it.name, teachers.firstOrNull { t -> t.name == it.teacher }) } }
            .distinct()
            .mapNotNull { courseRepository.getByIndiwareId(it).getFirstValue() }

        downloadedCourses.let {
            val existingCourseIds = existingCourses.map { it.id }
            val downloadedCoursesToDelete = existingCourseIds.filter { existingCourseId -> downloadedCourses.none { it.id == existingCourseId } }
            LOGGER.d { "Delete ${downloadedCoursesToDelete.size} courses" }
            courseRepository.deleteById(downloadedCoursesToDelete)
        }

        val updatedCourses = downloadedCourses.filter { downloadedCourse -> existingCourses.none { it.hashCode() == downloadedCourse.hashCode() } }
        LOGGER.d { "Upsert ${updatedCourses.size} courses" }
        courseRepository.upsert(updatedCourses)
    }

    private suspend fun updateSubjectInstances(
        baseData: IndiwareBaseData,
        existingSubjectInstances: List<SubjectInstance>,
    ) {
        val downloadedSubjectInstances = baseData.classes
            .flatMap { baseDataClass ->
                baseDataClass.subjectInstances.map { it.subjectInstanceNumber }
                    .distinct()
                    .mapNotNull { subjectInstanceRepository.getByIndiwareId(it).getFirstValue() }
            }

        downloadedSubjectInstances.let {
            val existingSubjectInstanceIds = existingSubjectInstances.map { it.id }
            val downloadedSubjectInstancesToDelete = existingSubjectInstanceIds.filter { existingSubjectInstanceId -> downloadedSubjectInstances.none { it.id == existingSubjectInstanceId } }
            LOGGER.d { "Delete ${downloadedSubjectInstancesToDelete.size} default lessons" }
            subjectInstanceRepository.deleteById(downloadedSubjectInstancesToDelete)
        }

        val updatedSubjectInstances = downloadedSubjectInstances.filter { downloadedSubjectInstance -> existingSubjectInstances.none { it.hashCode() == downloadedSubjectInstance.hashCode() } }
        LOGGER.d { "Upsert ${updatedSubjectInstances.size} default lessons" }
        subjectInstanceRepository.upsert(updatedSubjectInstances)
    }
}