package plus.vplan.app.core.common.domain.usecase

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.core.utils.date.now

class GetCurrentDateTimeUseCase {
    operator fun invoke() = flow {
        while (currentCoroutineContext().isActive) {
            emit(LocalDateTime.now())
            delay(50)
        }
    }
}