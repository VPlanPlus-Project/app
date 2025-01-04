package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.findByIndiwareId
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.latest
import kotlin.uuid.Uuid

private val LOGGER = Logger.withTag("UpdateSubstitutionPlanUseCase")

class UpdateSubstitutionPlanUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val weekRepository: WeekRepository,
    private val dayRepository: DayRepository,
    private val defaultLessonRepository: DefaultLessonRepository,
    private val lessonTimeRepository: LessonTimeRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository
) {
    suspend operator fun invoke(indiwareSchool: School.IndiwareSchool, date: LocalDate): Response.Error? {
        val teachers = teacherRepository.getBySchool(indiwareSchool.id).latest()
        val rooms = roomRepository.getBySchool(indiwareSchool.id).latest()
        val groups = groupRepository.getBySchool(indiwareSchool.id).latest()
        val defaultLessons = defaultLessonRepository.getBySchool(indiwareSchool.id).latest()
        val week = weekRepository.getBySchool(indiwareSchool.id).latest().firstOrNull { date in it.start..it.end } ?: return Response.Error.Other("Week for $date not found")

        val substitutionPlanResponse = indiwareRepository.getSubstitutionPlan(
            sp24Id = indiwareSchool.sp24Id,
            username = indiwareSchool.username,
            password = indiwareSchool.password,
            date = date,
            teacherNames = teachers.map { it.name },
            roomNames = rooms.map { it.name }
        )

        if (substitutionPlanResponse is Response.Error) return substitutionPlanResponse
        if (substitutionPlanResponse !is Response.Success) throw IllegalStateException("substitutionPlanResponse is not successful: $substitutionPlanResponse")

        val substitutionPlan = substitutionPlanResponse.data

        val day = Day(
            date = date,
            school = indiwareSchool,
            week = week,
            info = substitutionPlan.info
        )

        dayRepository.insert(day)

        substitutionPlan.classes.flatMap { substitutionPlanClass ->
            val group = groups.firstOrNull { it.name == substitutionPlanClass.name } ?: run {
                LOGGER.w { "Group ${substitutionPlanClass.name} not found" }
                return@flatMap emptyList()
            }
            val lessonTimes = lessonTimeRepository.getByGroup(group.id).latest()
            substitutionPlanClass.lessons.map { substitutionPlanLesson ->
                Lesson.SubstitutionPlanLesson(
                    id = Uuid.random().toHexString(),
                    date = date,
                    week = Cacheable.Loaded(week),
                    subject = substitutionPlanLesson.subject,
                    isSubjectChanged = substitutionPlanLesson.subjectChanged,
                    teachers = teachers.filter { it.name in substitutionPlanLesson.teacher }.map { Cacheable.Loaded(it) },
                    isTeacherChanged = substitutionPlanLesson.teacherChanged,
                    rooms = rooms.filter { it.name in substitutionPlanLesson.room }.map { Cacheable.Loaded(it) },
                    isRoomChanged = substitutionPlanLesson.roomChanged,
                    groups = listOf(group).map { Cacheable.Loaded(it) },
                    defaultLesson = substitutionPlanLesson.defaultLessonNumber?.let { defaultLessons.findByIndiwareId(it.toString()) }?.let { Cacheable.Loaded(it) },
                    lessonTime = lessonTimes.first { it.lessonNumber == substitutionPlanLesson.lessonNumber }.let { Cacheable.Loaded(it) },
                    info = substitutionPlanLesson.info
                )
            }
        }.let { lessons ->
            substitutionPlanRepository.insertNewSubstitutionPlan(indiwareSchool.id, lessons)
        }

        return null
    }
}