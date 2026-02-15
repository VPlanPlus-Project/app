package plus.vplan.app.feature.system.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppIdRepository

class SendSp24CredentialsToServerUseCase(
    private val profileRepository: ProfileRepository,
    private val vppIdRepository: VppIdRepository
) {
    companion object {
        private val logger = Logger.withTag("SendSp24CredentialsToServerUseCase")
    }

    suspend operator fun invoke() {
        profileRepository
            .getAll().first()
            .map { it.school }
            .distinctBy { it.id }
            .filter { it.credentialsValid }
            .also { logger.i { "Logging credentials for ${it.size} school${if (it.size != 1) "s" else ""}" } }
            .forEach { school ->
                vppIdRepository.logSp24Credentials(school.buildSp24AppAuthentication())
            }
    }
}