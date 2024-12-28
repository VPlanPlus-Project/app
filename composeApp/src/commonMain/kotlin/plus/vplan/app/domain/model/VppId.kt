package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDateTime

sealed class VppId {
    abstract val id: Int
    abstract val name: String
    abstract val groups: List<Group>
    abstract val cachedAt: LocalDateTime

    data class Cached(
        override val id: Int,
        override val name: String,
        override val groups: List<Group>,
        override val cachedAt: LocalDateTime
    ) : VppId()

    data class Active(
        override val id: Int,
        override val name: String,
        override val groups: List<Group>,
        override val cachedAt: LocalDateTime,
        val accessToken: String,
        val schulverwalterAccessToken: String?
    ) : VppId()

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