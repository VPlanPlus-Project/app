package plus.vplan.app.feature.profile.settings.page.main.domain.usecase

import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.ui.util.openUrl

class ConnectVppIdUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val vppIdRepository: VppIdRepository,
) {
    suspend operator fun invoke(profile: Profile.StudentProfile) {
        keyValueRepository.set(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE, profile.id.toHexString())
        openUrl(vppIdRepository.getAuthUrl())
    }
}