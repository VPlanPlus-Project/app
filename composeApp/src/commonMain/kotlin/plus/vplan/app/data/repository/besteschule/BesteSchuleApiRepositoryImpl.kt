package plus.vplan.app.data.repository.besteschule

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.besteschule.api.ApiStudentData
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.besteschule.BesteSchuleApiRepository
import kotlin.time.Duration.Companion.minutes

class BesteSchuleApiRepositoryImpl : BesteSchuleApiRepository, KoinComponent {
    private val httpClient by inject<HttpClient>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val studentDataCache = mutableMapOf<String, ApiStudentData>()
    override suspend fun getStudentData(schulverwalterAccessToken: String, withCache: Boolean): Response<ApiStudentData> {
        if (withCache && studentDataCache.containsKey(schulverwalterAccessToken)) return Response.Success(studentDataCache[schulverwalterAccessToken]!!)
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "students")
                    parameters.append("pagination", "false")
                    parameters.append("include", "intervals,subjects,grades")
                }
                bearerAuth(schulverwalterAccessToken)
            }

            if (!response.status.isSuccess()) return response.toErrorResponse()
            val data = response.body<ResponseDataWrapper<List<ApiStudentData>>>().data.first()
            studentDataCache[schulverwalterAccessToken] = data
            scope.launch {
                delay(1.minutes)
                studentDataCache.remove(schulverwalterAccessToken)
            }
            return Response.Success(data)
        }

        return Response.Error.Cancelled
    }

    private val studentGradeDataCache = mutableMapOf<String, List<ApiStudentGradesData>>()
    override suspend fun getStudentGradeData(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData>> {
        if (studentGradeDataCache.containsKey(schulverwalterAccessToken)) return Response.Success(studentGradeDataCache[schulverwalterAccessToken]!!)
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "grades")
                    parameters.append("include", "collection")
                    parameters.append("pagination", "false")
                }
                bearerAuth(schulverwalterAccessToken)
            }

            if (!response.status.isSuccess()) return response.toErrorResponse()
            val data = response.body<ResponseDataWrapper<List<ApiStudentGradesData>>>().data
            studentGradeDataCache[schulverwalterAccessToken] = data
            scope.launch {
                delay(1.minutes)
                studentGradeDataCache.remove(schulverwalterAccessToken)
            }
            return Response.Success(data)
        }

        return Response.Error.Cancelled
    }

    override fun clearApiCache() {
        studentDataCache.clear()
        studentGradeDataCache.clear()
    }

    override suspend fun setYearForUser(
        schulverwalterAccessToken: String,
        yearId: Int?
    ): Response<Unit> {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    port = 443
                    pathSegments = listOf("api", "years", "current")
                }
                bearerAuth(schulverwalterAccessToken)

                contentType(ContentType.Application.Json)
                setBody(SetYearRequest(yearId))
            }
            if (!response.status.isSuccess()) return response.toErrorResponse()
            return Response.Success(Unit)
        }
        return Response.Error.Cancelled
    }
}

@Serializable
private data class SetYearRequest(
    @SerialName("id") val yearId: Int?
)