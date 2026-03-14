package plus.vplan.app.feature.grades.common.domain.usecase

import kotlinx.coroutines.runBlocking
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.platform.BiometricAuthentication

class RequestGradeUnlockUseCase(
    private val runBiometricAuthentication: BiometricAuthentication,
    private val keyValueRepository: KeyValueRepository
) {
    operator fun invoke(
        onSuccess: () -> Unit = {}
    ) {
        runBiometricAuthentication.run(
            title = "Noten entsperren",
            subtitle = "Bitte bestätige deine Identität, um die Noten zu entsperren.",
            negativeButtonText = "Abbrechen",
            onSuccess = { runBlocking { keyValueRepository.set(Keys.GRADES_LOCKED, "false") }; onSuccess() },
            onError = { },
            onCancel = { }
        )
    }
}