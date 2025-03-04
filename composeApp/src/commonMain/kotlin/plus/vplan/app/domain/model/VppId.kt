package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.model.schulverwalter.Grade

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
        val schulverwalterConnection: SchulverwalterConnection?,
        val gradeIds: List<Int>
    ) : VppId() {
        fun buildSchoolApiAccess(schoolId: Int = -1): SchoolApiAccess.VppIdAccess {
            return SchoolApiAccess.VppIdAccess(schoolId, accessToken, id)
        }

        val grades by lazy { combine(gradeIds.map { App.gradeSource.getById(it).filterIsInstance<CacheState.Done<Grade>>().map { it.data } }) { it.toList() } }

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