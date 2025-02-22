package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.Item

sealed class VppId : Item {
    abstract val id: Int
    abstract val name: String
    abstract val groups: List<Int>
    abstract val cachedAt: Instant

    override fun getEntityId(): String = this.id.toString()

    data class Cached(
        override val id: Int,
        override val name: String,
        override val groups: List<Int>,
        override val cachedAt: Instant
    ) : VppId()

    data class Active(
        override val id: Int,
        override val name: String,
        override val groups: List<Int>,
        override val cachedAt: Instant,
        val accessToken: String,
        val schulverwalterConnection: SchulverwalterConnection?
    ) : VppId() {
        fun buildSchoolApiAccess(schoolId: Int = -1): SchoolApiAccess.VppIdAccess {
            return SchoolApiAccess.VppIdAccess(schoolId, accessToken, id)
        }

        data class SchulverwalterConnection(
            val accessToken: String,
            val userId: Int
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