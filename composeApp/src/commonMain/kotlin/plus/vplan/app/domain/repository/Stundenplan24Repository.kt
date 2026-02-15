package plus.vplan.app.domain.repository

import plus.vplan.app.core.model.Response
import plus.vplan.lib.sp24.model.splan.student.SPlanStudentData
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.extension.LessonTime

interface Stundenplan24Repository {
    suspend fun checkCredentials(authentication: Authentication): Response<Boolean>
    suspend fun downloadLessonTimes(authentication: Authentication, contextWeekIndex: Int?): plus.vplan.lib.sp24.source.Response<List<LessonTime>>

    suspend fun getWPlanSplan(authentication: Authentication, weekIndex: Int): plus.vplan.lib.sp24.source.Response<SPlanStudentData>

    suspend fun getSp24Client(authentication: Authentication, withCache: Boolean): Stundenplan24Client
}
