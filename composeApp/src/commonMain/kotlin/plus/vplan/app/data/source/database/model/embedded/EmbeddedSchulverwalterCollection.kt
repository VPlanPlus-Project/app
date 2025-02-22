package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterSubject
import plus.vplan.app.domain.model.schulverwalter.Collection

data class EmbeddedSchulverwalterCollection(
    @Embedded val collection: DbSchulverwalterCollection,
    @Relation(
        parentColumn = "id",
        entityColumn = "collection_id",
        entity = FKSchulverwalterCollectionSchulverwalterInterval::class
    ) val interval: FKSchulverwalterCollectionSchulverwalterInterval,

    @Relation(
        parentColumn = "id",
        entityColumn = "collection_id",
        entity = FKSchulverwalterCollectionSchulverwalterSubject::class
    ) val subject: FKSchulverwalterCollectionSchulverwalterSubject
) {
    fun toModel() = Collection(
        id = collection.id,
        type = collection.type,
        name = collection.name,
        intervalId = interval.intervalId,
        teacherId = 0,
        subjectId = subject.subjectId,
        cachedAt = collection.cachedAt
    )
}