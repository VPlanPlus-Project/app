package plus.vplan.app.network.vpp.assessment

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.core.model.application.network.ApiException
import plus.vplan.app.core.model.application.network.NetworkRequestUnsuccessfulException
import plus.vplan.app.network.besteschule.ResponseDataWrapper

class AssessmentApiImpl(
    private val httpClient: HttpClient
) : AssessmentApi {
    private val baseUrl = "https://vplan.plus/api/app/assessment/v1"

    override suspend fun getAssessments(
        schoolApiAccess: VppSchoolAuthentication,
        subjectInstanceAliases: List<Alias>
    ): List<ApiAssessmentDto> {
        try {
            val response = httpClient.get(baseUrl) {
                schoolApiAccess.authentication(this)
                url {
                    if (subjectInstanceAliases.isNotEmpty()) {
                        parameters.append("filter_subject_instances", subjectInstanceAliases.joinToString(","))
                    }
                    parameters.append("include_files", "true")
                }
            }
            if (response.status != HttpStatusCode.OK) throw NetworkRequestUnsuccessfulException(response)
            return response.body<ResponseDataWrapper<List<ApiAssessmentDto>>>().data
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }

    override suspend fun getAssessmentById(
        schoolApiAccess: VppSchoolAuthentication,
        assessmentId: Int
    ): ApiAssessmentDto? {
        try {
            val response = httpClient.get("$baseUrl/$assessmentId") {
                schoolApiAccess.authentication(this)
                url {
                    parameters.append("include_files", "true")
                }
            }
            if (!response.status.isSuccess()) return null
            return response.body<ResponseDataWrapper<ApiAssessmentDto>>().data
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }

    override suspend fun createAssessment(
        vppId: VppId.Active,
        request: AssessmentPostRequest
    ): AssessmentPostResponse {
        try {
            val response = httpClient.post(baseUrl) {
                vppId.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status != HttpStatusCode.OK) throw NetworkRequestUnsuccessfulException(response)
            return response.body<ResponseDataWrapper<AssessmentPostResponse>>().data
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }

    override suspend fun updateAssessment(
        vppId: VppId.Active,
        assessmentId: Int,
        request: AssessmentPatchRequest
    ) {
        try {
            val response = httpClient.patch("$baseUrl/$assessmentId") {
                vppId.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }

    override suspend fun deleteAssessment(
        vppId: VppId.Active,
        assessmentId: Int
    ) {
        try {
            val response = httpClient.delete("$baseUrl/$assessmentId") {
                vppId.buildVppSchoolAuthentication().authentication(this)
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }

    override suspend fun linkFile(
        vppId: VppId.Active,
        assessmentId: Int,
        fileId: Int
    ) {
        try {
            val response = httpClient.post("$baseUrl/$assessmentId/file") {
                vppId.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(AssessmentFileLinkRequest(fileId))
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }

    override suspend fun unlinkFile(
        vppId: VppId.Active,
        assessmentId: Int,
        fileId: Int
    ) {
        try {
            val response = httpClient.delete("$baseUrl/$assessmentId/file/$fileId") {
                vppId.buildVppSchoolAuthentication().authentication(this)
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }
}
