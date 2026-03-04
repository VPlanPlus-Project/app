package plus.vplan.app.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import plus.vplan.app.core.database.VppDatabase

class KeyValueRepositoryImpl(
    private val vppDatabase: VppDatabase
) : KeyValueRepository {
    override suspend fun set(key: String, value: String) {
        vppDatabase.keyValueDao.set(key, value)
    }

    override suspend fun delete(key: String) {
        vppDatabase.keyValueDao.delete(key)
    }

    override fun get(key: String): Flow<String?> {
        return vppDatabase.keyValueDao.get(key).distinctUntilChanged()
    }
}