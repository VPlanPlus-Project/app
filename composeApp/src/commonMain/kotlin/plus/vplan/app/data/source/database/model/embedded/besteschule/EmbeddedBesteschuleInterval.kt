package plus.vplan.app.data.source.database.model.embedded.besteschule

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleCollection
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleInterval
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleIntervalUser
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval

data class EmbeddedBesteschuleInterval(
    @Embedded val interval: DbBesteSchuleInterval,
    @Relation(
        parentColumn = "id",
        entityColumn = "interval_id",
        entity = DbBesteschuleIntervalUser::class
    ) val linkedAccountIds: List<DbBesteschuleIntervalUser>,
    @Relation(
        parentColumn = "id",
        entityColumn = "interval_id",
        entity = DbBesteSchuleCollection::class
    ) val collections: List<DbBesteSchuleCollection>
) {
    fun toModel() = BesteSchuleInterval(
        id = this.interval.id,
        type = BesteSchuleInterval.Type.fromString(this.interval.type),
        name = this.interval.name,
        from = this.interval.from,
        to = this.interval.to,
        includedIntervalId = this.interval.includedIntervalId,
        yearId = this.interval.yearId,
        linkedToSchulverwalterAccountIds = this.linkedAccountIds.map { it.schulverwalterUserId }.toSet(),
        collectionIds = collections.map { it.id }.toSet(),
        cachedAt = interval.cachedAt
    )
}