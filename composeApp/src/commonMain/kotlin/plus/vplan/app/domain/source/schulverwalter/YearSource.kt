package plus.vplan.app.domain.source.schulverwalter

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.schulverwalter.Year

class YearSource(

) {
    private val cache = hashMapOf<Int, Flow<Year>>()

    fun getById(id: Int): Flow<Year> {
        return cache.getOrPut(id) { TODO() }
    }

    fun getAll(): Flow<List<Year>> {
        return TODO()
    }
}