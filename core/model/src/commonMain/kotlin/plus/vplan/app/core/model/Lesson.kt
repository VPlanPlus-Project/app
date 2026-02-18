package plus.vplan.app.core.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

sealed interface Lesson : Item<Uuid, DataTag> {
    val weekId: String?
    val subject: String?
    val teacherIds: List<Uuid>
    val roomIds: List<Uuid>?
    val groupIds: List<Uuid>
    val subjectInstanceId: Uuid?
    val lessonNumber: Int
    val lessonTimeId: String?

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
        override val teacherIds: List<Uuid>,
        override val roomIds: List<Uuid>?,
        override val groupIds: List<Uuid>,
        override val lessonNumber: Int,
        override val lessonTimeId: String?,
        val timetableId: Uuid,
        val weekType: String?,
        val limitedToWeekIds: Set<String>?
    ) : Lesson {
        override val subjectInstanceId = null
        override val isCancelled: Boolean = false

        override fun getLessonSignature(): String {
            return "$subject/$teacherIds/$roomIds/$groupIds/$lessonNumber/$dayOfWeek/$weekType"
        }
    }

    data class SubstitutionPlanLesson(
        override val id: Uuid,
        val date: LocalDate,
        override val weekId: String?,
        override val subject: String?,
        val isSubjectChanged: Boolean,
        override val teacherIds: List<Uuid>,
        val isTeacherChanged: Boolean,
        override val roomIds: List<Uuid>,
        val isRoomChanged: Boolean,
        override val groupIds: List<Uuid>,
        override val subjectInstanceId: Uuid?,
        override val lessonNumber: Int,
        override val lessonTimeId: String?,
        val info: String?
    ) : Lesson {
        override val isCancelled: Boolean
            get() = subject == null && subjectInstanceId != null

        override fun getLessonSignature(): String {
            return "$subject/${teacherIds.sorted()}/${roomIds.sorted()}/${groupIds.sorted()}/$lessonNumber/$date/$subjectInstanceId"
        }
    }
}