package plus.vplan.app.domain.repository.base

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Item

interface ItemRepository<ID, I: Item<ID, *>> {
    fun getByLocalId(id: ID): Flow<I?>
    fun getAllLocalIds(): Flow<List<ID>>
}