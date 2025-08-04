package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbIndiwareTimetableMetadata
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.lib.sp24.model.splan.student.SPlanStudentData
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.IndiwareClient
import plus.vplan.lib.sp24.source.TestConnectionResult
import plus.vplan.lib.sp24.source.extension.LessonTime

class IndiwareRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : IndiwareRepository {

    val clients = mutableMapOf<Authentication, IndiwareClient>()

    override suspend fun checkCredentials(
        authentication: Authentication
    ): Response<Boolean> {
        val client = clients.getOrPut(authentication) { IndiwareClient(authentication, httpClient) }
        val result = client.testConnection()
        if (result is TestConnectionResult.NotFound) return Response.Error.OnlineError.NotFound
        if (result is TestConnectionResult.Unauthorized) return Response.Success(false)
        if (result is TestConnectionResult.Success) return Response.Success(true)
        return Response.Error.Other(result.toString())
    }

    override suspend fun downloadLessonTimes(
        authentication: Authentication,
        contextWeekIndex: Int?
    ): plus.vplan.lib.sp24.source.Response<List<LessonTime>> {
        val client = clients.getOrPut(authentication) { IndiwareClient(authentication, httpClient) }
        return client.lessonTime.getLessonTime(contextWeekIndex)
    }

    override suspend fun getWPlanSplan(
        authentication: Authentication,
        weekIndex: Int
    ): plus.vplan.lib.sp24.source.Response<SPlanStudentData> {
        val client = clients.getOrPut(authentication) { IndiwareClient(authentication, httpClient) }
        return client.getSPlanDataStudent(authentication, schoolWeekIndex = weekIndex)
    }

    override suspend fun getSp24Client(
        authentication: Authentication,
        withCache: Boolean
    ): IndiwareClient {
        if (withCache) return IndiwareClient(
            authentication = authentication,
            client = httpClient,
            enableInternalCache = true
        )
        return clients.getOrPut(authentication) {
            IndiwareClient(
                authentication = authentication,
                client = httpClient,
                enableInternalCache = false
            )
        }
    }

    override suspend fun hasTimetableForWeek(sp24SchoolId: Int, weekId: String): Boolean? {
        return vppDatabase.indiwareDao.getHasTimetableInWeek(weekId, sp24SchoolId.toString())?.hasData
    }

    override suspend fun setHasTimetableForWeek(
        sp24SchoolId: Int,
        weekId: String,
        hasTimetable: Boolean,
    ) {
        vppDatabase.indiwareDao.upsert(DbIndiwareTimetableMetadata(sp24SchoolId.toString(), weekId, hasTimetable))
    }
}