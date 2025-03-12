package plus.vplan.app

import plus.vplan.app.feature.sync.domain.usecase.FullSyncUseCase

@Suppress("unused") // Called in Swift App
suspend fun sync() {
    val useCase: FullSyncUseCase = getKoinInstance()
    useCase.invoke()
}