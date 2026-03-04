package plus.vplan.app.core.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

sealed interface Lesson : Item<Uuid, DataTag> {
    val weekId: String?
    val subject: String?
    val teachers: List<Teacher>
    val rooms: List<Room>?
    val groups: List<Group>
    val subjectInstance: SubjectInstance?
    val lessonNumber: Int
    val lessonTime: LessonTime?

    fun getLessonSignature(): String

    override val tags: Set<DataTag>
        get() = emptySet()

    val isCancelled: Boolean

    /**
     * @param weekId The ID of the week when the corresponding timetable starts to be valid.
     */
    data class TimetableLesson(
        override val id: Uuid,
        val dayOfWeek: DayOfWeek,
        override val weekId: String,
        override val subject: String?,
        override val teachers: List<Teacher>,
        override val rooms: List<Room>?,
        override val groups: List<Group>,
        override val lessonNumber: Int,
        override val lessonTime: LessonTime?,
        val timetableId: Uuid,
        val weekType: String?,
        val limitedToWeeks: List<Week>?
    ) : Lesson {
        override val subjectInstance = null
        override val isCancelled: Boolean = false

        override fun getLessonSignature(): String {
            return "$subject/${teachers.map { it.id }.sorted()}/${rooms.orEmpty().map { it.id }.sorted()}/${groups.map { it.id }.sorted()}/$lessonNumber/$dayOfWeek/$weekType"
        }
    }

    data class SubstitutionPlanLesson(
        override val id: Uuid,
        val date: LocalDate,
        override val weekId: String?,
        override val subject: String?,
        val isSubjectChanged: Boolean,
        override val teachers: List<Teacher>,
        val isTeacherChanged: Boolean,
        override val rooms: List<Room>,
        val isRoomChanged: Boolean,
        override val groups: List<Group>,
        override val subjectInstance: SubjectInstance?,
        override val lessonNumber: Int,
        override val lessonTime: LessonTime?,
        val info: String?
    ) : Lesson {
        override val isCancelled: Boolean
            get() = subject == null && subjectInstance != null

        override fun getLessonSignature(): String {
            return "$subject/${teachers.map { it.id }.sorted()}/${rooms.map { it.id }.sorted()}/${groups.map { it.id }.sorted()}/$lessonNumber/$date/${subjectInstance?.id}"
        }
    }
}