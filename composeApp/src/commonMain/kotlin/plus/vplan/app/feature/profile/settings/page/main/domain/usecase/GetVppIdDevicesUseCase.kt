package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.core.data.vpp_id.VppIdDevice
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.VppId

class GetVppIdDevicesUseCase(
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(vppId: VppId.Active): List<VppIdDevice> {
        return vppIdRepository.getDevices(vppId)
    }
}
