package plus.vplan.app.domain.repository

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School

interface IndiwareRepository {
    suspend fun checkCredentials(sp24Id: Int, username: String, password: String): Response<Boolean>

    suspend fun getBaseData(
        sp24Id: String,
        username: String,
        password: String
    ): Response<IndiwareBaseData>
}

data class IndiwareBaseData(
    val classes: List<Class>,
    val holidays: List<LocalDate>,
    val teachers: List<String>,
    val rooms: List<String>,
    val daysPerWeek: Int,
    val studentsHaveFullAccess: Boolean,
    val downloadMode: School.IndiwareSchool.SchoolDownloadMode
) {
    data class Class(
        val name: String,
        val lessonTimes: List<LessonTime>,
        val defaultLessons: List<DefaultLesson>
    ) {
        data class LessonTime(
            val start: LocalTime,
            val end: LocalTime,
            val lessonNumber: Int
        )

        data class DefaultLesson(
            val subject: String,
            val teacher: String,
            val defaultLessonNumber: String,
            val course: Course?
        )

        data class Course(
            val name: String,
            val teacher: String
        )
    }

    data class Teacher(
        val name: String
    )

    data class Room(
        val name: String
    )
}