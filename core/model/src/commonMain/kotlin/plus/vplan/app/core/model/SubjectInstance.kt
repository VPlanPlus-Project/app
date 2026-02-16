package plus.vplan.app.core.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class SubjectInstance(
    override val id: Uuid,
    val subject: String,
    val courseId: Uuid?,
    val teacherId: Uuid?,
    val groupIds: List<Uuid>,
    val cachedAt: Instant,
    override val aliases: Set<Alias>
) : AliasedItem<DataTag> {
    override val tags: Set<DataTag> = emptySet()

    companion object {
        fun buildSp24Alias(sp24SchoolId: Int, sp24VpId: Int): Alias {
            return Alias(
                provider = AliasProvider.Sp24,
                value = "$sp24SchoolId/${sp24VpId}",
                version = 1
            )
        }
    }
}