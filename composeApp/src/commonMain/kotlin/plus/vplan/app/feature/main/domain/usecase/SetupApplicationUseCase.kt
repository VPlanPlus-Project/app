package plus.vplan.app.feature.main.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.BuildConfig
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.feature.main.domain.usecase.setup.DoAssessmentsAndHomeworkIndexMigrationUseCase
import plus.vplan.app.feature.main.domain.usecase.setup.RemoveDisconnectedVppIdsFromProfilesUseCase

class SetupApplicationUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val doAssessmentsAndHomeworkIndexMigrationUseCase: DoAssessmentsAndHomeworkIndexMigrationUseCase,
    private val updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase,
    private val removeDisconnectedVppIdsFromProfilesUseCase: RemoveDisconnectedVppIdsFromProfilesUseCase
) {
    private val logger = Logger.withTag("SetupApplicationUseCase")
    suspend operator fun invoke() {
        removeDisconnectedVppIdsFromProfilesUseCase()
        updateFirebaseTokenUseCase()
        if (keyValueRepository.get(Keys.PREVIOUS_APP_VERSION).first() != BuildConfig.APP_VERSION_CODE.toString()) {
            logger.i { "First run of VPlanPlus" }
            logger.d { "Saving migration flags" }

            keyValueRepository.set(Keys.MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES, "true")
        }

        if (keyValueRepository.get(Keys.MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES).first() != "true") {
            logger.i { "Migrating assessments and homework indices" }
            doAssessmentsAndHomeworkIndexMigrationUseCase()
        }

        keyValueRepository.set(Keys.PREVIOUS_APP_VERSION, BuildConfig.APP_VERSION_CODE.toString())
    }
}