package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.NetworkErrorKind
import plus.vplan.app.core.model.NetworkException
import plus.vplan.app.core.model.VppId

class CheckIfVppIdIsStillConnectedUseCase(
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(vppId: VppId.Active): VppIdConnectionState {
        return try {
            val isValid = vppIdRepository.checkTokenValidity(vppId.accessToken)
            if (isValid) VppIdConnectionState.CONNECTED else VppIdConnectionState.DISCONNECTED
        } catch (e: NetworkException) {
            if (e.kind == NetworkErrorKind.Unauthorized) VppIdConnectionState.DISCONNECTED
            else VppIdConnectionState.ERROR
        } catch (e: Exception) {
            VppIdConnectionState.ERROR
        }
    }
}

enum class VppIdConnectionState {
    CONNECTED, DISCONNECTED, ERROR, UNKNOWN
}
