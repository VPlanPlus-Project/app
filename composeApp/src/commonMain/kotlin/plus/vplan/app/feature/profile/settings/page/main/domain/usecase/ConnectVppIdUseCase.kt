package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.utils.openUrl

class ConnectVppIdUseCase(
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(profile: Profile.StudentProfile) {
        keyValueRepository.set(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE, profile.id.toHexString())
        openUrl(VPP_ID_AUTH_URL)
    }
}