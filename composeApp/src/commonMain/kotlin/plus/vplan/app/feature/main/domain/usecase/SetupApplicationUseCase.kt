package plus.vplan.app.feature.main.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.App
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.feature.main.domain.usecase.setup.DoAssessmentsAndHomeworkIndexMigrationUseCase

class SetupApplicationUseCase(
    private val keyValueRepository: KeyValueRepository,
    private val doAssessmentsAndHomeworkIndexMigrationUseCase: DoAssessmentsAndHomeworkIndexMigrationUseCase
) {
    private val logger = Logger.withTag("SetupApplicationUseCase")
    suspend operator fun invoke() {
        if (keyValueRepository.get(Keys.PREVIOUS_APP_VERSION).first() != App.versionCode.toString()) {
            logger.i { "First run of VPlanPlus" }
            logger.d { "Saving migration flags" }

            keyValueRepository.set(Keys.MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES, "true")
        }

        if (keyValueRepository.get(Keys.MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES).first() != "true") {
            logger.i { "Migrating assessments and homework indices" }
            doAssessmentsAndHomeworkIndexMigrationUseCase()
        }

        keyValueRepository.set(Keys.PREVIOUS_APP_VERSION, App.versionCode.toString())
    }
}