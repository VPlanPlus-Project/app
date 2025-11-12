package plus.vplan.app.feature.settings.page.developer.logs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual fun getLogs(): Flow<List<Log>> {
    return flowOf(emptyList())
}