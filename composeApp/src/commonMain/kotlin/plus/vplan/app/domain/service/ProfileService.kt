package plus.vplan.app.domain.service

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Profile

interface ProfileService {
    fun getCurrentProfile(): Flow<Profile?>
}