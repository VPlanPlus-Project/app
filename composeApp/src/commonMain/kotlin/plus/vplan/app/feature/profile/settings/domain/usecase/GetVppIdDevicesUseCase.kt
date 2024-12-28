package plus.vplan.app.feature.profile.settings.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdDevice
import plus.vplan.app.domain.repository.VppIdRepository

class GetVppIdDevicesUseCase(
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(vppId: VppId.Active): Response<List<VppIdDevice>> {
        return vppIdRepository.getDevices(vppId)
    }
}