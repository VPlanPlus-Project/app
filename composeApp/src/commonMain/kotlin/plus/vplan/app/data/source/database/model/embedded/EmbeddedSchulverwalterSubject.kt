package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterSubjectSchulverwalterFinalGrade
import plus.vplan.app.domain.model.schulverwalter.Subject

data class EmbeddedSchulverwalterSubject(
    @Embedded val subject: DbSchulverwalterSubject,
    @Relation(
        parentColumn = "id",
        entityColumn = "subject_id",
        entity = FKSchulverwalterCollectionSchulverwalterSubject::class
    ) val collections: List<FKSchulverwalterCollectionSchulverwalterSubject>,
    @Relation(
        parentColumn = "id",
        entityColumn = "subject_id",
        entity = FKSchulverwalterSubjectSchulverwalterFinalGrade::class
    ) val finalGrade: FKSchulverwalterSubjectSchulverwalterFinalGrade?
) {
    fun toModel() = Subject(
        id = subject.id,
        name = subject.name,
        localId = subject.localId,
        collectionIds = collections.map { it.collectionId },
        finalGradeId = finalGrade?.finalGradeId,
        cachedAt = subject.cachedAt
    )
}
