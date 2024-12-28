package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.DbVppIdAccess
import plus.vplan.app.data.source.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.data.source.database.model.database.crossovers.DbVppIdGroupCrossover
import plus.vplan.app.domain.model.VppId

data class EmbeddedVppId(
    @Embedded val vppId: DbVppId,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DbVppIdGroupCrossover::class,
            parentColumn = "vpp_id",
            entityColumn = "group_id"
        ),
        entity = DbGroup::class
    ) val groups: List<EmbeddedGroup>,
    @Relation(
        parentColumn = "id",
        entityColumn = "vpp_id",
        entity = DbVppIdAccess::class
    ) val access: DbVppIdAccess?,
    @Relation(
        parentColumn = "id",
        entityColumn = "vpp_id",
        entity = DbVppIdSchulverwalter::class
    ) val schulverwalterAccess: DbVppIdSchulverwalter?
) {
    fun toModel(): VppId {
        if (access == null) return VppId.Cached(
            id = vppId.id,
            name = vppId.name,
            groups = groups.map { it.toModel() },
            cachedAt = vppId.cachedAt
        )
        return VppId.Active(
            id = vppId.id,
            name = vppId.name,
            groups = groups.map { it.toModel() },
            accessToken = access.accessToken,
            cachedAt = vppId.cachedAt,
            schulverwalterAccessToken = schulverwalterAccess?.schulverwalterAccessToken
        )
    }
}