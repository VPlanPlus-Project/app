package plus.vplan.app.feature.onboarding

import kotlinx.coroutines.CompletableDeferred

interface IosDevInfoSheetHandler {
    fun showSheet(onDismiss: () -> Unit)
}

/**
 * Suspends until the sheet has been shown and dismissed.
 */
internal suspend fun IosDevInfoSheetHandler.awaitSheet() {
    val deferred = CompletableDeferred<Unit>()
    showSheet { deferred.complete(Unit) }
    deferred.await()
}
