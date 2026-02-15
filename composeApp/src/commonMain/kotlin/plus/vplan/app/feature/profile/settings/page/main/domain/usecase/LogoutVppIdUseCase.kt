package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository

class LogoutVppIdUseCase(
    private val vppIdRepository: VppIdRepository,
    private val profileRepository: ProfileRepository,
): KoinComponent {
    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()

    suspend operator fun invoke(vppId: VppId.Active): Response<Unit> {
        val result = vppIdRepository.logout(vppId.accessToken)
        if (result is Response.Error && result !is Response.Error.OnlineError.Unauthorized) return result
        profileRepository
            .getAll()
            .first()
            .filterIsInstance<Profile.StudentProfile>()
            .forEach {
                if (it.vppId?.id == vppId.id) profileRepository.updateVppId(it.id, null)
            }
        val schulverwalterUserId = vppId.schulverwalterConnection?.userId
        if (schulverwalterUserId != null) {
            besteSchuleGradesRepository.clearCacheForUser(schulverwalterUserId)
        }
        vppIdRepository.deleteAccessTokens(vppId)
        return Response.Success(Unit)
    }
}