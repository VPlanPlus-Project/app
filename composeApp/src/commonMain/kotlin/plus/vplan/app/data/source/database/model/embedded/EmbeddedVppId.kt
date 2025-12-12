package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbVppId
import plus.vplan.app.data.source.database.model.database.DbVppIdAccess
import plus.vplan.app.data.source.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.data.source.database.model.database.crossovers.DbVppIdGroupCrossover
import plus.vplan.app.domain.model.VppId

data class EmbeddedVppId(
    @Embedded val vppId: DbVppId,
    @Relation(
        parentColumn = "id",
        entityColumn = "vpp_id",
        entity = DbVppIdGroupCrossover::class
    ) val groups: List<DbVppIdGroupCrossover>,
    @Relation(
        parentColumn = "id",
        entityColumn = "vpp_id",
        entity = DbVppIdAccess::class
    ) val access: DbVppIdAccess?,
    @Relation(
        parentColumn = "id",
        entityColumn = "vpp_id",
        entity = DbVppIdSchulverwalter::class
    ) val schulverwalterAccess: DbVppIdSchulverwalter?,
) {
    fun toModel(): VppId {
        if (access == null) return VppId.Cached(
            id = vppId.id,
            name = vppId.name,
            groups = groups.map { it.groupId },
            cachedAt = vppId.cachedAt
        )
        return VppId.Active(
            id = vppId.id,
            name = vppId.name,
            groups = groups.map { it.groupId },
            accessToken = access.accessToken,
            cachedAt = vppId.cachedAt,
            schulverwalterConnection =
                if (schulverwalterAccess == null) null
                else VppId.Active.SchulverwalterConnection(
                    accessToken = schulverwalterAccess.schulverwalterAccessToken,
                    userId = schulverwalterAccess.schulverwalterUserId,
                    isValid = schulverwalterAccess.isValid
                )
        )
    }
}