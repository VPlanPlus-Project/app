package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.utils.latest

class LogoutVppIdUseCase(
    private val vppIdRepository: VppIdRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(vppId: VppId.Active): Response<Unit> {
        val result = vppIdRepository.logout(vppId.accessToken)
        if (result is Response.Error && result !is Response.Error.OnlineError.Unauthorized) return result
        profileRepository
            .getAll()
            .latest()
            .filterIsInstance<Profile.StudentProfile>()
            .forEach {
                if (it.vppId == vppId.id) profileRepository.updateVppId(it.id, null)
            }
        vppIdRepository.deleteAccessTokens(vppId)
        return Response.Success(Unit)
    }
}