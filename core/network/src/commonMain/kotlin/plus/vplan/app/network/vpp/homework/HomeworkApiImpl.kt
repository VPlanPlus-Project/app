package plus.vplan.app.network.vpp.homework

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.network.ApiException
import plus.vplan.app.network.NetworkRequestUnsuccessfulException
import plus.vplan.app.network.besteschule.ResponseDataWrapper

class HomeworkApiImpl(
    private val httpClient: HttpClient
) : HomeworkApi {
    private val baseUrl = "https://vplan.plus/api/app/homework/v1"

    override suspend fun getHomeworkItems(
        access: VppSchoolAuthentication,
        filterGroups: List<String>?,
        filterSubjectInstances: List<String>?
    ): List<ApiHomeworkDto> {
        try {
            val response = httpClient.get(baseUrl) {
                access.authentication(this)
                url {
                    filterGroups.orEmpty().let { parameters.append("filter_groups", it.joinToString(",")) }
                    filterSubjectInstances.orEmpty().let { parameters.append("filter_subject_instances", it.joinToString(",")) }
                    parameters.append("include_tasks", "true")
                    parameters.append("include_files", "true")
                    parameters.append("include_subject_instances", "true")
                }
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
            return response.body<ResponseDataWrapper<List<ApiHomeworkDto>>>().data
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun getHomeworkById(vppId: VppId.Active, homeworkId: Int): ApiHomeworkDto? {
        try {
            val response = httpClient.get("$baseUrl/$homeworkId") {
                bearerAuth(vppId.accessToken)
                url {
                    parameters.append("include_tasks", "true")
                    parameters.append("include_files", "true")
                }
            }
            if (!response.status.isSuccess()) return null
            return response.body<ResponseDataWrapper<ApiHomeworkDto>>().data
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun createHomework(vppId: VppId.Active, request: HomeworkPostRequest): HomeworkPostResponse {
        try {
            val response = httpClient.post(baseUrl) {
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
            return response.body<ResponseDataWrapper<HomeworkPostResponse>>().data
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun updateHomework(vppId: VppId.Active, homeworkId: Int, request: HomeworkPatchRequest) {
        try {
            val response = httpClient.patch("$baseUrl/$homeworkId") {
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun deleteHomework(vppId: VppId.Active, homeworkId: Int) {
        try {
            val response = httpClient.delete("$baseUrl/$homeworkId") {
                bearerAuth(vppId.accessToken)
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun addTask(vppId: VppId.Active, homeworkId: Int, content: String): Int {
        try {
            val response = httpClient.post("$baseUrl/$homeworkId/task") {
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                setBody(HomeworkAddTaskRequest(content))
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
            return response.body<ResponseDataWrapper<Int>>().data
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun updateTask(vppId: VppId.Active, homeworkId: Int, taskId: Int, content: String?, isDone: Boolean?) {
        try {
            val response = httpClient.patch("$baseUrl/$homeworkId/task/$taskId") {
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                if (isDone != null) setBody(HomeworkTaskUpdateDoneStateRequest(isDone))
                else if (content != null) setBody(HomeworkTaskUpdateContentRequest(content))
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun deleteTask(vppId: VppId.Active, homeworkId: Int, taskId: Int) {
        try {
            val response = httpClient.delete("$baseUrl/$homeworkId/task/$taskId") {
                bearerAuth(vppId.accessToken)
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun linkFile(vppId: VppId.Active, homeworkId: Int, fileId: Int) {
        try {
            val response = httpClient.post("$baseUrl/$homeworkId/file") {
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                setBody(HomeworkFileLinkRequest(fileId))
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun unlinkFile(vppId: VppId.Active, homeworkId: Int, fileId: Int) {
        try {
            val response = httpClient.delete("$baseUrl/$homeworkId/file") {
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                setBody(HomeworkFileLinkRequest(fileId))
            }
            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }
}
