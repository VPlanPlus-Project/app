package plus.vplan.app.network.vpp.file

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.core.model.application.network.ApiException
import plus.vplan.app.core.model.application.network.NetworkRequestUnsuccessfulException
import plus.vplan.app.network.besteschule.ResponseDataWrapper

class FileApiImpl(
    private val httpClient: HttpClient
) : FileApi {
    
    private val baseUrl = "https://vplan.plus/api/app/file/v1"
    
    override suspend fun uploadFile(
        vppId: VppId.Active,
        fileName: String,
        fileBytes: ByteArray,
        onProgress: (Float) -> Unit
    ): Int {
        try {
            val response = httpClient.post(baseUrl) {
                vppId.buildVppSchoolAuthentication().authentication(this)
                header("File-Name", fileName)
                header(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                header(HttpHeaders.ContentLength, fileBytes.size.toString())
                setBody(ByteReadChannel(fileBytes))
                onUpload { bytesSentTotal, contentLength ->
                    val progress = if (contentLength != null) {
                        (bytesSentTotal.toFloat() / contentLength.toFloat())
                    } else 0f
                    onProgress(progress)
                }
            }

            if (!response.status.isSuccess()) {
                throw NetworkRequestUnsuccessfulException(response)
            }

            return response.body<ResponseDataWrapper<Int>>().data
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }
    
    override suspend fun downloadFile(
        fileId: Int,
        schoolApiAccess: VppSchoolAuthentication,
        onProgress: (Float) -> Unit
    ): ByteArray {
        try {
            val response = httpClient.get("$baseUrl/$fileId/download") {
                schoolApiAccess.authentication(this)
                onDownload { bytesSentTotal, contentLength ->
                    val progress = if (contentLength == null) 0f
                    else (bytesSentTotal.toFloat() / contentLength.toFloat())
                    onProgress(progress)
                }
            }

            if (!response.status.isSuccess()) {
                throw NetworkRequestUnsuccessfulException(response)
            }

            return response.bodyAsBytes()
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }
    
    override suspend fun renameFile(
        fileId: Int,
        newName: String,
        vppId: VppId.Active
    ) {
        try {
            val response = httpClient.patch("$baseUrl/$fileId") {
                vppId.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(FileUpdateNameRequest(newName))
            }

            if (!response.status.isSuccess()) {
                throw NetworkRequestUnsuccessfulException(response)
            }
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }
    
    override suspend fun deleteFile(
        fileId: Int,
        vppId: VppId.Active
    ) {
        try {
            val response = httpClient.delete("$baseUrl/$fileId") {
                vppId.buildVppSchoolAuthentication().authentication(this)
            }

            if (!response.status.isSuccess()) {
                throw NetworkRequestUnsuccessfulException(response)
            }
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }
}

@Serializable
internal data class FileUpdateNameRequest(
    @SerialName("file_name") val fileName: String,
)
