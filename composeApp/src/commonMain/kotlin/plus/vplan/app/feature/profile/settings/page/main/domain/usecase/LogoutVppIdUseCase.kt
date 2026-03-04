package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.GradesRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.NetworkErrorKind
import plus.vplan.app.core.model.NetworkException
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId

class LogoutVppIdUseCase(
    private val vppIdRepository: VppIdRepository,
    private val profileRepository: ProfileRepository,
): KoinComponent {
    private val besteSchuleGradesRepository by inject<GradesRepository>()

    suspend operator fun invoke(vppId: VppId.Active) {
        try {
            vppIdRepository.logout(vppId.accessToken)
        } catch (e: NetworkException) {
            if (e.kind != NetworkErrorKind.Unauthorized) throw e
            // Unauthorized means already logged out — continue cleanup
        }
        profileRepository
            .getAll()
            .first()
            .filterIsInstance<Profile.StudentProfile>()
            .forEach { profile ->
                if (profile.vppId?.id == vppId.id) {
                    profile.copy(vppId = vppId).let { profileRepository.save(it) }
                }
            }
        val schulverwalterUserId = vppId.schulverwalterConnection?.userId
        if (schulverwalterUserId != null) {
            besteSchuleGradesRepository.removeGradesForUser(schulverwalterUserId)
        }
        vppIdRepository.deleteAccessTokens(vppId)
    }
}
