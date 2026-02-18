package plus.vplan.app.domain.model.populated

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Room
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.core.model.Week
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.WeekRepository
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
        val lessonTimes = when (context) {
            is PopulationContext.Profile if context.profile is Profile.StudentProfile -> lessonTimeRepository.getByGroup(
                context.profile.group.id
            )

            is PopulationContext.Profile -> lessonTimeRepository.getBySchool(context.profile.school.id)
            is PopulationContext.School -> lessonTimeRepository.getBySchool(context.school.id)
        }

        val subjectInstances = when (context) {
            is PopulationContext.Profile if context.profile is Profile.StudentProfile -> subjectInstanceRepository.getByGroup(
                context.profile.group.id
            )

            is PopulationContext.Profile -> subjectInstanceRepository.getBySchool(context.profile.school.id)
            is PopulationContext.School -> subjectInstanceRepository.getBySchool(context.school.id)
        }

        val groups = when (context) {
            is PopulationContext.Profile -> groupRepository.getBySchool(context.profile.school.id)
            is PopulationContext.School -> groupRepository.getBySchool(context.school.id)
        }

        val rooms = when (context) {
            is PopulationContext.Profile -> roomRepository.getBySchool(context.profile.school.id)
            is PopulationContext.School -> roomRepository.getBySchool(context.school.id)
        }

        val teachers = when (context) {
            is PopulationContext.Profile -> teacherRepository.getBySchool(context.profile.school.id)
            is PopulationContext.School -> teacherRepository.getBySchool(context.school.id)
        }

        val weeks = when (context) {
            is PopulationContext.Profile -> weekRepository.getBySchool(context.profile.school.id)
            is PopulationContext.School -> weekRepository.getBySchool(context.school.id)
        }

        return combine6(
            lessonTimes,
            subjectInstances,
            groups,
            rooms,
            teachers,
            weeks
        ) { lessonTimes, subjectInstances, groups, rooms, teachers, weeks ->
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
                        weeks = lesson.limitedToWeekIds?.let { weeks.filter { it.id in lesson.limitedToWeekIds!! } }
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

        val groups = groupRepository.getBySchool(contextSchool.id)
        val rooms = roomRepository.getBySchool(contextSchool.id)
        val teachers = teacherRepository.getBySchool(contextSchool.id)
        val weeks = weekRepository.getBySchool(contextSchool.id)

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