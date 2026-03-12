package plus.vplan.app.core.database.model.embedded.besteschule

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleCollection
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleInterval
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleSubject
import plus.vplan.app.core.model.besteschule.BesteSchuleCollection

data class EmbeddedBesteSchuleCollection(
    @Embedded val collection: DbBesteSchuleCollection,
    @Relation(
        parentColumn = "subject_id",
        entityColumn = "id",
        entity = DbBesteSchuleSubject::class
    ) val subject: DbBesteSchuleSubject,
    @Relation(
        parentColumn = "interval_id",
        entityColumn = "id",
        entity = DbBesteSchuleInterval::class
    ) val interval: EmbeddedBesteSchuleInterval,
) {
    fun toModel() = BesteSchuleCollection(
        id = this.collection.id,
        type = this.collection.type,
        name = this.collection.name,
        subject = this.subject.toModel(),
        givenAt = this.collection.givenAt,
        interval = this.interval.toModel(),
        teacherId = this.collection.teacherId,
        cachedAt = this.collection.cachedAt,
    )
}
