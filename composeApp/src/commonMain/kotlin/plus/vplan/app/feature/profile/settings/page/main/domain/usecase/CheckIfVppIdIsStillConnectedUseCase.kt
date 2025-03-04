package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class CheckIfVppIdIsStillConnectedUseCase(
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(vppId: VppId.Active): VppIdConnectionState {
        val response = vppIdRepository.getUserByToken(vppId.accessToken, upsert = false)
        if (response is Response.Success && response.data.id == vppId.id) return VppIdConnectionState.CONNECTED
        if (response is Response.Error.OnlineError.Unauthorized) return VppIdConnectionState.DISCONNECTED
        return VppIdConnectionState.ERROR
    }
}

enum class VppIdConnectionState {
    CONNECTED, DISCONNECTED, ERROR, UNKNOWN
}