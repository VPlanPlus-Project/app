package plus.vplan.app.feature.settings.page.info.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class SendFeedbackUseCase(
    private val vppIdRepository: VppIdRepository,
    private val getFeedbackMetadataUseCase: GetFeedbackMetadataUseCase
) {
    suspend operator fun invoke(profile: Profile, email: String?, message: String?): Response<Unit> {
        return vppIdRepository.sendFeedback(
            access = if (profile is Profile.StudentProfile && profile.vppIdId != null) (profile.vppId!!.getFirstValue() as VppId.Active).buildSchoolApiAccess()
            else profile.getSchool().getFirstValue()!!.getSchoolApiAccess()!!,
            content = message + "\n\n" + getFeedbackMetadataUseCase().first().toString(),
            email = email
        )
    }
}