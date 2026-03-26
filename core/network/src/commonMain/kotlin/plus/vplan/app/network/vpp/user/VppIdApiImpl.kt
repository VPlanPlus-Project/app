package plus.vplan.app.network.vpp.user

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.model.application.network.ApiException
import plus.vplan.app.core.model.application.network.NetworkRequestUnsuccessfulException
import plus.vplan.app.network.besteschule.ResponseDataWrapper
import plus.vplan.app.network.vpp.GenericAuthenticationProvider
import plus.vplan.app.network.vpp.getAuthenticationOptionsForRestrictedEntity

class VppIdApiImpl(
    private val httpClient: HttpClient,
    private val genericAuthenticationProvider: GenericAuthenticationProvider,
) : VppIdApi {

    override suspend fun getById(id: Int): VppIdDto? {
        try {
            val url = URLBuilder("https://vplan.plus/api/app").apply {
                appendPathSegments("user", "v1", id.toString())
            }.buildString()
            val authenticationOptions = getAuthenticationOptionsForRestrictedEntity(
                httpClient = httpClient,
                url = url
            ) ?: return null

            val authentication = genericAuthenticationProvider.getAuthentication(authenticationOptions)
                ?: return null

            val response = httpClient.get(url) {
                authentication.authentication(this)
            }

            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

            return response.body<ResponseDataWrapper<UserItemResponse>>()
                .data
                .toDto()
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }
}

@Serializable
private data class UserItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val username: String,
) {
    fun toDto() = VppIdDto(
        id = this.id,
        name = this.username,
    )
}