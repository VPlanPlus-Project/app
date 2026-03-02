package plus.vplan.app.network.vpp.group

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.model.Alias
import plus.vplan.app.network.besteschule.NetworkRequestUnsuccessfulException
import plus.vplan.app.network.besteschule.ResponseDataWrapper
import plus.vplan.app.network.vpp.GenericAuthenticationProvider
import plus.vplan.app.network.vpp.getAuthenticationOptionsForRestrictedEntity
import plus.vplan.app.network.vpp.model.ApiAlias
import plus.vplan.app.network.vpp.model.IncludedModel

class GroupApiImpl(
    private val httpClient: HttpClient,
    private val genericAuthenticationProvider: GenericAuthenticationProvider,
): GroupApi {
    override suspend fun getById(identifier: Alias): VppGroupDto? {
        val url = URLBuilder("https://vplan.plus/api/app").apply {
            appendPathSegments("group", "v1", identifier.toUrlString())
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

        return response.body<ResponseDataWrapper<GroupItemResponse>>()
            .data
            .toDto()
    }
}

@Serializable
private data class GroupItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("school") val school: IncludedModel,
    @SerialName("name") val name: String,
    @SerialName("aliases") val aliases: List<ApiAlias>
) {
    fun toDto() = VppGroupDto(
        id = this.id,
        schoolId = this.school.id,
        name = this.name,
        aliases = this.aliases.map { it.toDto() }
    )
}
