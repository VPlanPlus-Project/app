package plus.vplan.app.network.vpp

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.database.dao.VppIdDao
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication

class VppIdAuthenticationProvider(
    private val vppIdDao: VppIdDao
) {
    suspend fun getAuthenticationForVppId(vppId: Int): VppSchoolAuthentication.Vpp? {
        return (vppIdDao.getById(vppId).first()?.toModel() as? VppId.Active)?.buildVppSchoolAuthentication()
    }
}