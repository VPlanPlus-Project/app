package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdDevice
import plus.vplan.app.domain.repository.VppIdRepository

class LogoutVppIdDeviceUseCase(
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(vppId: VppId.Active, device: VppIdDevice): Response<Unit> {
        return vppIdRepository.logoutDevice(vppId, device.id)
    }
}