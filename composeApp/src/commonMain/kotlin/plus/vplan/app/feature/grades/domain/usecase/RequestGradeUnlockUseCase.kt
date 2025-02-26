package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.runBlocking
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.ui.platform.RunBiometricAuthentication

class RequestGradeUnlockUseCase(
    private val runBiometricAuthentication: RunBiometricAuthentication,
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