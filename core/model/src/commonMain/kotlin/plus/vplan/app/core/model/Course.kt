package plus.vplan.app.core.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Course(
    override val id: Uuid,
    val groupIds: List<Uuid>,
    val name: String,
    val teacherId: Uuid?,
    val cachedAt: Instant,
    override val aliases: Set<Alias>
): AliasedItem<DataTag> {
    override val tags: Set<DataTag> = emptySet()

    companion object {
        fun buildSp24Alias(
            sp24SchoolId: String,
            name: String,
            classNames: Set<String>,
            teacherName: String?
        ) = Alias(
            provider = AliasProvider.Sp24,
            value = "$sp24SchoolId.$name+${teacherName ?: ""}+${classNames.sorted().joinToString("|")}",
            version = 1
        )
    }
}