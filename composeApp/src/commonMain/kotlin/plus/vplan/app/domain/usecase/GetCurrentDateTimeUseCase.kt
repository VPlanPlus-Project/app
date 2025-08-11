package plus.vplan.app.domain.usecase

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.utils.now

class GetCurrentDateTimeUseCase {
    operator fun invoke() = flow {
        while (currentCoroutineContext().isActive) {
            emit(LocalDateTime.now())
            kotlinx.coroutines.delay(50)
        }
    }
}