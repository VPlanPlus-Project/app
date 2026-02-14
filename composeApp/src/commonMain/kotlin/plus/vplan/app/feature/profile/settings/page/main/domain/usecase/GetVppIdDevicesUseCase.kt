package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.core.model.Response
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