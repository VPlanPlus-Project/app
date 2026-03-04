package plus.vplan.app.feature.settings.page.info.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.Profile

class SendFeedbackWithProfileUseCase(
    private val vppIdRepository: VppIdRepository,
    private val getFeedbackMetadataUseCase: GetFeedbackMetadataUseCase
) {
    suspend operator fun invoke(profile: Profile, email: String?, message: String?) {
        vppIdRepository.sendFeedback(
            access =
                if (profile is Profile.StudentProfile && profile.vppId != null) profile.vppId!!.buildVppSchoolAuthentication()
                else profile.school.buildSp24AppAuthentication(),
            content = message + "\n\n" + getFeedbackMetadataUseCase().first().toString(),
            email = email
        )
    }
}
