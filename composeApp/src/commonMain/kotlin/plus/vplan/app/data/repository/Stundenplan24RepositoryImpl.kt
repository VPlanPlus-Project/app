package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbStundenplan24TimetableMetadata
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.lib.sp24.model.splan.student.SPlanStudentData
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.TestConnectionResult
import plus.vplan.lib.sp24.source.extension.LessonTime

class Stundenplan24RepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : Stundenplan24Repository {

    val clients = mutableMapOf<Authentication, Stundenplan24Client>()

    override suspend fun checkCredentials(
        authentication: Authentication
    ): Response<Boolean> {
        val client = clients.getOrPut(authentication) { Stundenplan24Client(authentication, httpClient) }
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
        val client = clients.getOrPut(authentication) { Stundenplan24Client(authentication, httpClient) }
        return client.lessonTime.getLessonTime(contextWeekIndex)
    }

    override suspend fun getWPlanSplan(
        authentication: Authentication,
        weekIndex: Int
    ): plus.vplan.lib.sp24.source.Response<SPlanStudentData> {
        val client = clients.getOrPut(authentication) { Stundenplan24Client(authentication, httpClient) }
        return client.getSPlanDataStudent(authentication, schoolWeekIndex = weekIndex)
    }

    override suspend fun getSp24Client(
        authentication: Authentication,
        withCache: Boolean
    ): Stundenplan24Client {
        if (withCache) return Stundenplan24Client(
            authentication = authentication,
            client = httpClient,
            enableInternalCache = true
        )
        return clients.getOrPut(authentication) {
            Stundenplan24Client(
                authentication = authentication,
                client = httpClient,
                enableInternalCache = false
            )
        }
    }

    override suspend fun hasTimetableForWeek(sp24SchoolId: String, weekId: String): Stundenplan24Repository.HasData {
        return when (vppDatabase.stundenplan24Dao.getHasTimetableInWeek(weekId, sp24SchoolId)?.hasData) {
            true -> Stundenplan24Repository.HasData.Yes
            false -> Stundenplan24Repository.HasData.No
            null -> Stundenplan24Repository.HasData.Unknown
        }
    }

    override suspend fun setHasTimetableForWeek(
        sp24SchoolId: String,
        weekId: String,
        hasTimetable: Boolean,
    ) {
        vppDatabase.stundenplan24Dao.upsert(DbStundenplan24TimetableMetadata(sp24SchoolId, weekId, hasTimetable))
    }
}