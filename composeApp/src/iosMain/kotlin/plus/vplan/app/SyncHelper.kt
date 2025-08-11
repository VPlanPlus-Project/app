package plus.vplan.app

import plus.vplan.app.feature.sync.domain.usecase.fullsync.FullSyncCause
import plus.vplan.app.feature.sync.domain.usecase.fullsync.FullSyncUseCase

@Suppress("unused") // Called in Swift App
fun sync() {
    val useCase: FullSyncUseCase = getKoinInstance()
    useCase.invoke(FullSyncCause.Manual)
}