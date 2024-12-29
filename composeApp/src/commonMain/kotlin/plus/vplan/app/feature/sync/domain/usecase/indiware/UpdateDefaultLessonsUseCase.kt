package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareBaseData
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.utils.latest

private val LOGGER = Logger.withTag("UpdateDefaultLessonsUseCase")

class UpdateDefaultLessonsUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val defaultLessonRepository: DefaultLessonRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(school: School.IndiwareSchool): Response.Error? {
        val baseData = indiwareRepository.getBaseData(school.sp24Id, school.username, school.password)
        if (baseData is Response.Error) return baseData
        if (baseData !is Response.Success) throw IllegalStateException("baseData is not successful: $baseData")

        val groups = groupRepository.getBySchool(schoolId = school.id).first()
        val teachers = teacherRepository.getBySchool(schoolId = school.id).first()
        val existingCourses = courseRepository.getBySchool(schoolId = school.id)
        val existingDefaultLessons = defaultLessonRepository.getBySchool(schoolId = school.id)

        updateCourses(
            school = school,
            baseData = baseData.data,
            existingCourses = existingCourses.latest(),
            groups = groups,
            teachers = teachers
        )

        updateDefaultLessons(
            baseData = baseData.data,
            courses = existingCourses.latest(),
            existingDefaultLessons = existingDefaultLessons.latest(),
            groups = groups,
            teachers = teachers
        )

        return null
    }

    private suspend fun updateCourses(
        school: School.IndiwareSchool,
        baseData: IndiwareBaseData,
        existingCourses: List<Course>,
        groups: List<Group>,
        teachers: List<Teacher>
    ) {
        val downloadedCourses = baseData.classes.flatMap { baseDataClass ->
            val group = groups.firstOrNull { it.name == baseDataClass.name } ?: throw NoSuchElementException("Group ${baseDataClass.name} not found")
            baseDataClass.defaultLessons.mapNotNull { it.course }.map { course ->
                Course.fromIndiware(
                    sp24SchoolId = school.sp24Id,
                    groups = listOf(group),
                    name = course.name,
                    teacher = if (course.teacher.isNullOrBlank()) null else teachers.firstOrNull { it.name == course.teacher }
                )
            }
        }
            .groupBy { it.id }
            .map { (id, courses) ->
                Course(
                    id = id,
                    groups = courses.flatMap { it.groups }.distinctBy { it.id },
                    name = courses.first().name,
                    teacher = courses.first().teacher
                )
            }

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

    private suspend fun updateDefaultLessons(
        baseData: IndiwareBaseData,
        courses: List<Course>,
        existingDefaultLessons: List<DefaultLesson>,
        groups: List<Group>,
        teachers: List<Teacher>
    ) {
        val downloadedDefaultLessons = baseData.classes
            .flatMap { baseDataClass ->
                val group = groups.firstOrNull { it.name == baseDataClass.name } ?: throw NoSuchElementException("Group ${baseDataClass.name} not found")
                baseDataClass.defaultLessons.map { defaultLesson ->
                    DefaultLesson(
                        id = defaultLesson.defaultLessonNumber,
                        subject = defaultLesson.subject,
                        groups = listOf(group),
                        course = courses.firstOrNull { it.name == defaultLesson.course?.name },
                        teacher = teachers.firstOrNull { it.name == defaultLesson.teacher }
                    )
                }
            }
            .groupBy { it.id }
            .map { (id, defaultLessons) ->
                defaultLessonRepository.upsert(
                    DefaultLesson(
                        id = id,
                        subject = defaultLessons.first().subject,
                        groups = defaultLessons.flatMap { it.groups }.distinctBy { it.id },
                        course = defaultLessons.firstOrNull { it.course != null }?.course,
                        teacher = defaultLessons.firstOrNull { it.teacher != null }?.teacher
                    )
                ).first()
            }

        downloadedDefaultLessons.let {
            val existingDefaultLessonIds = existingDefaultLessons.map { it.id }
            val downloadedDefaultLessonsToDelete = existingDefaultLessonIds.filter { existingDefaultLessonId -> downloadedDefaultLessons.none { it.id == existingDefaultLessonId } }
            LOGGER.d { "Delete ${downloadedDefaultLessonsToDelete.size} default lessons" }
            defaultLessonRepository.deleteById(downloadedDefaultLessonsToDelete)
        }

        val updatedDefaultLessons = downloadedDefaultLessons.filter { downloadedDefaultLesson -> existingDefaultLessons.none { it.hashCode() == downloadedDefaultLesson.hashCode() } }
        LOGGER.d { "Upsert ${updatedDefaultLessons.size} default lessons" }
        defaultLessonRepository.upsert(updatedDefaultLessons)
    }
}