package plus.vplan.app.data.source.network

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.VppIdRepository

class VppIdAuthenticationProvider(
    private val vppIdRepository: VppIdRepository
) {
    suspend fun getAuthenticationForVppId(vppId: Int): VppSchoolAuthentication.Vpp? {
        return (vppIdRepository.getByLocalId(vppId).first() as? VppId.Active)?.buildVppSchoolAuthentication()
    }
}