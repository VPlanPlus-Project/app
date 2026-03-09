package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.model.Profile
import plus.vplan.app.getVppIdAuthUrl
import plus.vplan.app.utils.openUrl

class ConnectVppIdUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(profile: Profile.StudentProfile) {
        keyValueRepository.set(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE, profile.id.toHexString())
        openUrl(getVppIdAuthUrl())
    }
}