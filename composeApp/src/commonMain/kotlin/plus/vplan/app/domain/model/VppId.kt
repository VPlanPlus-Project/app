package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item
import kotlin.uuid.Uuid

sealed class VppId : Item<DataTag> {
    abstract val id: Int
    abstract val name: String
    abstract val groups: List<Uuid>
    abstract val cachedAt: Instant

    override fun getEntityId(): String = this.id.toString()
    override val tags: Set<DataTag> = emptySet()

    data class Cached(
        override val id: Int,
        override val name: String,
        override val groups: List<Uuid>,
        override val cachedAt: Instant
    ) : VppId()

    data class Active(
        override val id: Int,
        override val name: String,
        override val groups: List<Uuid>,
        override val cachedAt: Instant,
        val accessToken: String,
        val schulverwalterConnection: SchulverwalterConnection?,
        val gradeIds: List<Int>
    ) : VppId() {
        fun buildSchoolApiAccess(schoolId: Int = -1): SchoolApiAccess.VppIdAccess {
            return SchoolApiAccess.VppIdAccess(schoolId, accessToken, id)
        }

        val grades by lazy {
            if (gradeIds.isEmpty()) flowOf(emptyList())
            else combine(gradeIds.map { App.gradeSource.getById(it) }) { it.toList() }
        }

        data class SchulverwalterConnection(
            val accessToken: String,
            val userId: Int,
            val isValid: Boolean?
        )
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result += 31 * name.hashCode()
        result += 31 * groups.hashCode()
        result += 31 * cachedAt.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is VppId) return false

        if (this.hashCode() != other.hashCode()) return false

        return true
    }
}