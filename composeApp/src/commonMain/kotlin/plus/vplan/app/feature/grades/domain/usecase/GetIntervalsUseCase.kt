package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.schulverwalter.Interval

class GetIntervalsUseCase {
    suspend operator fun invoke(profile: Profile): Flow<List<Interval>> {
        if (profile !is Profile.StudentProfile) return flowOf(emptyList())
        val vppId = profile.vppId?.getFirstValueOld() as? VppId.Active ?: return flowOf(emptyList())
        val schulverwalterUserId = vppId.schulverwalterConnection?.userId ?: return flowOf(emptyList())
        return App.intervalSource.getForUser(schulverwalterUserId)
    }
}