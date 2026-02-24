package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.database.dao.VppIdDao
import plus.vplan.app.core.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.core.model.VppId
import plus.vplan.app.network.besteschule.BesteSchuleApi

class BesteSchuleRepositoryImpl(
    private val besteSchuleApi: BesteSchuleApi,
    private val vppIdDao: VppIdDao,
): BesteSchuleRepository {
    override suspend fun checkValidity(userId: Int): Boolean {
        val access = vppIdDao.getSchulverwalterAccess().first().first { it.schulverwalterUserId == userId }

        return besteSchuleApi.checkValidity(access.schulverwalterAccessToken)
    }

    override suspend fun saveBesteSchuleAccessValidity(userId: Int, valid: Boolean) {
        vppIdDao.setSchulverwalterValidity(valid, userId)
    }

    override suspend fun saveBesteSchuleAccess(
        userId: Int,
        vppId: VppId.Active,
        token: String
    ) {
        vppIdDao.upsert(DbVppIdSchulverwalter(
            vppId = vppId.id,
            schulverwalterUserId = userId,
            schulverwalterAccessToken = token,
            isValid = true
        ))
    }
}
