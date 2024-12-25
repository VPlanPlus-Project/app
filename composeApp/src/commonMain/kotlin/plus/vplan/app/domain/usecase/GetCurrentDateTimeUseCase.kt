package plus.vplan.app.domain.usecase

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetCurrentDateTimeUseCase {
    operator fun invoke() = flow {
        while (currentCoroutineContext().isActive) {
            emit(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
            kotlinx.coroutines.delay(50)
        }
    }
}