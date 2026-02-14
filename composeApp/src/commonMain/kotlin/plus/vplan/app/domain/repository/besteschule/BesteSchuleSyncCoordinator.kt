package plus.vplan.app.domain.repository.besteschule

import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade

interface BesteSchuleSyncCoordinator {

    suspend fun syncYears(schulverwalterUserId: Int)
    suspend fun syncBesteSchule(schulverwalterUserId: Int, yearId: Int): SyncResult

    class BesteSchuleUserDoesNotExistException: Exception("User does not exist")
}

data class SyncResult(
    val newGrades: List<BesteSchuleGrade>
)