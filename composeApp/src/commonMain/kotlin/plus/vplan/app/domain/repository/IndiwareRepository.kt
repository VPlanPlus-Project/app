package plus.vplan.app.domain.repository

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.data.Response
import plus.vplan.lib.sp24.model.splan.student.SPlanStudentData
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.IndiwareClient
import plus.vplan.lib.sp24.source.extension.LessonTime
import kotlin.uuid.Uuid

interface IndiwareRepository {
    suspend fun checkCredentials(authentication: Authentication): Response<Boolean>
    suspend fun downloadLessonTimes(authentication: Authentication, contextWeekIndex: Int?): plus.vplan.lib.sp24.source.Response<List<LessonTime>>

    suspend fun getWPlanSplan(authentication: Authentication, weekIndex: Int): plus.vplan.lib.sp24.source.Response<SPlanStudentData>

    suspend fun getSp24Client(authentication: Authentication, withCache: Boolean): IndiwareClient

    suspend fun hasTimetableForWeek(sp24SchoolId: Int, weekId: String): Boolean?
    suspend fun setHasTimetableForWeek(sp24SchoolId: Int, weekId: String, hasTimetable: Boolean)
}

data class IndiwareBaseData(
    val classes: List<Class>,
    val holidays: List<LocalDate>,
    val rooms: List<String>,
    val weeks: List<Week>?,
    val daysPerWeek: Int,
    val studentsHaveFullAccess: Boolean,
) {
    data class Class(
        val name: String,
    )

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
