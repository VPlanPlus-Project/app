package plus.vplan.app.domain.model.populated

import androidx.compose.runtime.Immutable
 import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.data.week.WeekRepository
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Room
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.core.model.Week
import plus.vplan.app.core.data.lesson_times.LessonTimeRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.utils.combine6

@Immutable
sealed class PopulatedLesson {
    abstract val lesson: Lesson
    abstract val lessonTime: LessonTime?
    abstract val groups: List<Group>
    abstract val rooms: List<Room>
    abstract val teachers: List<Teacher>

    data class TimetableLesson(
        override val lesson: Lesson.TimetableLesson,
        override val lessonTime: LessonTime?,
        override val groups: List<Group>,
        override val rooms: List<Room>,
        override val teachers: List<Teacher>,
        val weeks: List<Week>?
    ) : PopulatedLesson()

    data class SubstitutionPlanLesson(
        override val lesson: Lesson.SubstitutionPlanLesson,
        override val lessonTime: LessonTime?,
        override val groups: List<Group>,
        override val rooms: List<Room>,
        override val teachers: List<Teacher>,
        val subjectInstance: SubjectInstance?
    ) : PopulatedLesson()
}

class LessonPopulator : KoinComponent {
    private val logger = Logger.withTag("LessonPopulator")
    private val lessonTimeRepository by inject<LessonTimeRepository>()
    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()
    private val groupRepository by inject<GroupRepository>()
    private val roomRepository by inject<RoomRepository>()
    private val teacherRepository by inject<TeacherRepository>()
    private val weekRepository by inject<WeekRepository>()

    fun populateMultiple(
        lessons: List<Lesson>,
        context: PopulationContext
    ): Flow<List<PopulatedLesson>> {
        val tag = "populateMultiple[${lessons.size}]"
        logger.d { "$tag called – context=${context::class.simpleName}" }

        val lessonTimes: Flow<List<LessonTime>> = when (context) {
            is PopulationContext.Profile if context.profile is Profile.StudentProfile -> lessonTimeRepository.getByGroup(context.profile.group)
            is PopulationContext.Profile -> lessonTimeRepository.getBySchool(context.profile.school)
            is PopulationContext.School -> lessonTimeRepository.getBySchool(context.school)
        }.map { v -> v.also { logger.d { "$tag lessonTimes emitted: ${it.size}" } } }

        val subjectInstances: Flow<List<SubjectInstance>> = when (context) {
            is PopulationContext.Profile if context.profile is Profile.StudentProfile -> subjectInstanceRepository.getByGroup(context.profile.group)
            is PopulationContext.Profile -> subjectInstanceRepository.getBySchool(context.profile.school)
            is PopulationContext.School -> subjectInstanceRepository.getBySchool(context.school)
        }.map { v -> v.also { logger.d { "$tag subjectInstances emitted: ${it.size}" } } }

        val groups: Flow<List<Group>> = when (context) {
            is PopulationContext.Profile -> groupRepository.getBySchool(context.profile.school)
            is PopulationContext.School -> groupRepository.getBySchool(context.school)
        }.map { v -> v.also { logger.d { "$tag groups emitted: ${it.size}" } } }

        val rooms: Flow<List<Room>> = when (context) {
            is PopulationContext.Profile -> roomRepository.getBySchool(context.profile.school.id)
            is PopulationContext.School -> roomRepository.getBySchool(context.school.id)
        }.map { v -> v.also { logger.d { "$tag rooms emitted: ${it.size}" } } }

        val teachers: Flow<List<Teacher>> = when (context) {
            is PopulationContext.Profile -> teacherRepository.getBySchool(context.profile.school)
            is PopulationContext.School -> teacherRepository.getBySchool(context.school)
        }.map { v -> v.also { logger.d { "$tag teachers emitted: ${it.size}" } } }

        val weeks: Flow<List<Week>> = when (context) {
            is PopulationContext.Profile -> weekRepository.getBySchool(context.profile.school)
            is PopulationContext.School -> weekRepository.getBySchool(context.school)
        }.map { v -> v.also { logger.d { "$tag weeks emitted: ${it.size}" } } }

        return combine6(
            lessonTimes,
            subjectInstances,
            groups,
            rooms,
            teachers,
            weeks
        ) { lessonTimes, subjectInstances, groups, rooms, teachers, weeks ->
            logger.d { "$tag combine6 fired – lessonTimes=${lessonTimes.size} subjectInstances=${subjectInstances.size} groups=${groups.size} rooms=${rooms.size} teachers=${teachers.size} weeks=${weeks.size}" }
            lessons.map { lesson ->
                when (lesson) {
                    is Lesson.SubstitutionPlanLesson -> PopulatedLesson.SubstitutionPlanLesson(
                        lesson = lesson,
                        lessonTime = lessonTimes.firstOrNull { it.id == lesson.lessonTimeId },
                        groups = groups.filter { it.id in lesson.groupIds },
                        rooms = rooms.filter { it.id in lesson.roomIds },
                        teachers = teachers.filter { it.id in lesson.teacherIds },
                        subjectInstance = subjectInstances.firstOrNull { it.id == lesson.subjectInstanceId },
                    )
                    is Lesson.TimetableLesson -> PopulatedLesson.TimetableLesson(
                        lesson = lesson,
                        lessonTime = lessonTimes.firstOrNull { it.id == lesson.lessonTimeId },
                        groups = groups.filter { it.id in lesson.groupIds },
                        rooms = rooms.filter { it.id in lesson.roomIds.orEmpty() },
                        teachers = teachers.filter { it.id in lesson.teacherIds },
                        weeks = lesson.limitedToWeekIds?.let { limitedIds -> weeks.filter { it.id in limitedIds } }
                    )
                }
            }
        }.distinctUntilChanged()
    }

    fun populateSingle(lesson: Lesson, contextSchool: School): Flow<PopulatedLesson> {
        val lessonTime =
            lesson.lessonTimeId?.let { lessonTimeRepository.getById(it) } ?: flowOf(null)

        val subjectInstance =
            lesson.subjectInstanceId?.let { subjectInstanceRepository.getByLocalId(it) } ?: flowOf(
                null
            )

        val groups = groupRepository.getBySchool(contextSchool)
        val rooms = roomRepository.getBySchool(contextSchool.id)
        val teachers = teacherRepository.getBySchool(contextSchool)
        val weeks = weekRepository.getBySchool(contextSchool)

        return combine6(
            lessonTime,
            subjectInstance,
            groups,
            rooms,
            teachers,
            weeks
        ) { lessonTime, subjectInstance, groups, rooms, teachers, weeks ->
            when (lesson) {
                is Lesson.SubstitutionPlanLesson -> PopulatedLesson.SubstitutionPlanLesson(
                    lesson = lesson,
                    lessonTime = lessonTime,
                    groups = groups.filter { it.id in lesson.groupIds },
                    rooms = rooms.filter { it.id in lesson.roomIds },
                    teachers = teachers.filter { it.id in lesson.teacherIds },
                    subjectInstance = subjectInstance
                )
                is Lesson.TimetableLesson -> PopulatedLesson.TimetableLesson(
                    lesson = lesson,
                    lessonTime = lessonTime,
                    groups = groups.filter { it.id in lesson.groupIds },
                    rooms = rooms.filter { it.id in lesson.roomIds.orEmpty() },
                    teachers = teachers.filter { it.id in lesson.teacherIds },
                    weeks = lesson.limitedToWeekIds?.let { weeks.filter { it.id in lesson.limitedToWeekIds!! } }
                )
            }
        }.distinctUntilChanged()
    }
}