package plus.vplan.app.feature.main.domain.usecase

class SetupApplicationUseCase(
    private val updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase
) {
    suspend operator fun invoke() {
        updateFirebaseTokenUseCase()
    }
}