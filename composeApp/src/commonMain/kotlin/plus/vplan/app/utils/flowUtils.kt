package plus.vplan.app.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

suspend fun <T> Flow<T>.latest(): T = this.first()