package plus.vplan.app.feature.main.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.usecase.UpdateFirebaseTokenUseCase

expect suspend fun getFirebaseToken(): String?
expect suspend fun setProperty(property: String, value: String)

class UpdateFirebaseTokenUseCase(
    private val updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    private val keyValueRepository: KeyValueRepository
) {
    private val logger = Logger.withTag("UpdateFirebaseTokenUseCase[Main]")
    suspend operator fun invoke() {
        val token = keyValueRepository.get(Keys.FIREBASE_TOKEN).first()?.ifEmpty { null } ?: run {
            logger.w { "No firebase token found, requesting current..." }
            val newToken = getFirebaseToken()
            if (newToken == null) {
                logger.e { "No firebase token found, aborting update" }
                return
            }
            newToken
        }
        updateFirebaseTokenUseCase(token)
    }
}