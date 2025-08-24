package plus.vplan.app.domain.model

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import kotlin.uuid.Uuid

sealed class AppEntity : KoinComponent {
    val vppIdRepository by inject<VppIdRepository>()
    val profileRepository by inject<ProfileRepository>()

    data class VppId(val id: Int) : AppEntity() {
        val vppId by lazy { vppIdRepository.getById(id, ResponsePreference.Fast) }
        fun vppId(responsePreference: ResponsePreference) = vppIdRepository.getById(id, responsePreference)
    }
    data class Profile(val id: Uuid) : AppEntity() {
        val profile by lazy { profileRepository.getById(id) }
    }
}