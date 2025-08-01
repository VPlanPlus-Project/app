package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.model.Profile
import kotlin.uuid.Uuid

class GetProfileByIdUseCase {
    operator fun invoke(id: Uuid): Flow<Profile?> = App.profileSource.getById(id).map { if (it is CacheStateOld.NotExisting) null else (it as CacheStateOld.Done).data }
}