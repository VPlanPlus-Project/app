package plus.vplan.app.feature.settings.page.info.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class SendFeedbackWithProfileUseCase(
    private val vppIdRepository: VppIdRepository,
    private val getFeedbackMetadataUseCase: GetFeedbackMetadataUseCase
) {
    suspend operator fun invoke(profile: Profile, email: String?, message: String?): Response<Unit> {
        return vppIdRepository.sendFeedback(
            access = if (profile is Profile.StudentProfile && profile.vppIdId != null) (profile.vppId!!.getFirstValueOld() as VppId.Active).buildVppSchoolAuthentication()
            else profile.getSchool().getFirstValue()!!.buildSp24AppAuthentication(),
            content = message + "\n\n" + getFeedbackMetadataUseCase().first().toString(),
            email = email
        )
    }
}