package plus.vplan.app.domain.repository

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.lib.sp24.model.splan.student.SPlanStudentData
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.IndiwareClient
import plus.vplan.lib.sp24.source.extension.LessonTime

interface IndiwareRepository {
    suspend fun checkCredentials(authentication: Authentication): Response<Boolean>
    suspend fun downloadLessonTimes(authentication: Authentication, contextWeekIndex: Int?): plus.vplan.lib.sp24.source.Response<List<LessonTime>>

    suspend fun getBaseData(authentication: Authentication): Response<IndiwareBaseData>
    suspend fun getWPlanSplan(authentication: Authentication, weekIndex: Int): plus.vplan.lib.sp24.source.Response<SPlanStudentData>

    suspend fun getTimetable(
        sp24Id: String,
        username: String,
        password: String,
        week: Week,
        roomNames: List<String>
    ): Response<IndiwareTimeTable>

    suspend fun getSubstitutionPlan(
        sp24Id: String,
        username: String,
        password: String,
        date: LocalDate,
        teacherNames: List<String>,
        roomNames: List<String>
    ): Response<IndiwareSubstitutionPlan>

    suspend fun getSp24Client(authentication: Authentication, withCache: Boolean): IndiwareClient
}

data class IndiwareBaseData(
    val classes: List<Class>,
    val holidays: List<LocalDate>,
    val teachers: List<String>,
    val rooms: List<String>,
    val weeks: List<Week>?,
    val daysPerWeek: Int,
    val studentsHaveFullAccess: Boolean,
    val downloadMode: School.IndiwareSchool.SchoolDownloadMode
) {
    data class Class(
        val name: String,
        val lessonTimes: List<LessonTime>,
        val subjectInstances: List<SubjectInstance>
    ) {
        data class LessonTime(
            val start: LocalTime,
            val end: LocalTime,
            val lessonNumber: Int
        )

        data class SubjectInstance(
            val subject: String,
            val teacher: String?,
            val subjectInstanceNumber: String,
            val course: Course?
        )

        data class Course(
            val name: String,
            val teacher: String?
        )

    }

    data class Week(
        val calendarWeek: Int,
        val start: LocalDate,
        val end: LocalDate,
        val weekType: String,
        val weekIndex: Int,
    )

    data class Teacher(
        val name: String
    )

    data class Room(
        val name: String
    )
}

data class IndiwareTimeTable(
    val classes: List<Class>,
    val hasChangedToPrevious: Boolean
) {
    data class Class(
        val name: String,
        val lessons: List<Lesson>
    ) {
        data class Lesson(
            val dayOfWeek: DayOfWeek,
            val lessonNumber: Int,
            val subject: String,
            val teacher: List<String>,
            val room: List<String>,
            val weekType: String?
        )
    }
}

data class IndiwareSubstitutionPlan(
    val classes: List<Class>,
    val info: String?,
    val date: LocalDate
) {
    data class Class(
        val name: String,
        val lessons: List<Lesson>
    ) {
        data class Lesson(
            val lessonNumber: Int,
            val subject: String?,
            val subjectChanged: Boolean,
            val teacher: List<String>,
            val teacherChanged: Boolean,
            val room: List<String>,
            val roomChanged: Boolean,
            val info: String?,
            val subjectInstanceNumber: Int?,
            val start: LocalTime?,
            val end: LocalTime?
        )
    }
}