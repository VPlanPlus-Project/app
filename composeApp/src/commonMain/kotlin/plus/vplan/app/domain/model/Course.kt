package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasedItem
import kotlin.uuid.Uuid

data class Course(
    override val id: Uuid,
    val groups: List<Uuid>,
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
            teacher: Teacher?
        ) = "sp24.$sp24SchoolId.$name+${teacher?.name ?: ""}+${classNames.sorted().joinToString("|")}"
    }

    val teacher by lazy { teacherId?.let { App.teacherSource.getById(it) } }
}