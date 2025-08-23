package plus.vplan.app.data.source.network

import kotlinx.coroutines.flow.first
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.VppSchoolAuthentication

class VppIdAuthenticationProvider(
    private val vppDatabase: VppDatabase
) {
    suspend fun getAuthenticationForVppId(vppId: Int): VppSchoolAuthentication.Vpp? {
        return (vppDatabase.vppIdDao.getById(vppId).first()?.toModel() as? VppId.Active)?.buildVppSchoolAuthentication()
    }
}