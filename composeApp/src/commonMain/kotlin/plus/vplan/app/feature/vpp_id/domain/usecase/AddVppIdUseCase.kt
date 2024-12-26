package plus.vplan.app.feature.vpp_id.domain.usecase

import co.touchlab.kermit.Logger
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

private val logger = Logger.withTag("AddVppIdUseCase")

class AddVppIdUseCase(
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(token: String): Response<VppId.Active> {
        val accessToken = vppIdRepository.getAccessToken(token)
        if (accessToken is Response.Error) {
            logger.e { "Error getting access token: $accessToken" }
            return accessToken
        }
        if (accessToken !is Response.Success) throw IllegalStateException("Unexpected response type")
        return vppIdRepository.getUserByToken(accessToken.data)
    }
}