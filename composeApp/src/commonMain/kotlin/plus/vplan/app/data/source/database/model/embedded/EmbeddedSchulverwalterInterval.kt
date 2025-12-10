package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterIntervalUser
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterYearSchulverwalterInterval
import plus.vplan.app.domain.model.schulverwalter.Interval

data class EmbeddedSchulverwalterInterval(
    @Embedded val interval: DbSchulverwalterInterval,
    @Relation(
        parentColumn = "id",
        entityColumn = "interval_id",
        entity = FKSchulverwalterYearSchulverwalterInterval::class
    ) val year: FKSchulverwalterYearSchulverwalterInterval,
    @Relation(
        parentColumn = "id",
        entityColumn = "interval_id",
        entity = FKSchulverwalterCollectionSchulverwalterInterval::class
    ) val collections: List<FKSchulverwalterCollectionSchulverwalterInterval>,
    @Relation(
        parentColumn = "id",
        entityColumn = "interval_id",
        entity = DbSchulverwalterIntervalUser::class
    ) val users: List<DbSchulverwalterIntervalUser>
) {
    fun toModel() = Interval(
        id = interval.id,
        name = interval.name,
        type = Interval.Type.fromString(interval.type),
        from = interval.from,
        to = interval.to,
        includedIntervalId = interval.includedIntervalId,
        yearId = year.yearId,
        collectionIds = collections.map { it.collectionId },
        cachedAt = interval.cachedAt,
        linkedWithSchulverwalterUserIds = users.map { it.schulverwalterUserId }.toSet(),
    )
}