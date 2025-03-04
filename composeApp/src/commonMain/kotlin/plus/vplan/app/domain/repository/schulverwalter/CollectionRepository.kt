package plus.vplan.app.domain.repository.schulverwalter

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Collection
import plus.vplan.app.domain.repository.WebEntityRepository

interface CollectionRepository: WebEntityRepository<Collection> {
    suspend fun download(): Response<Set<Int>>
}