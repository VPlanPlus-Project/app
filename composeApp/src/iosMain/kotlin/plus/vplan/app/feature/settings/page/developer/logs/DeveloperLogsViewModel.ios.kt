package plus.vplan.app.feature.settings.page.developer.logs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual fun getLogs(): Flow<Log> {
    return emptyFlow()
}

actual fun clearLogs() {}