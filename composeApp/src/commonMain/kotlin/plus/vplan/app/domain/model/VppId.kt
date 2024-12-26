package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDateTime

sealed interface VppId {
    val id: Int
    val name: String
    val groups: List<Group>
    val cachedAt: LocalDateTime

    data class Cached(
        override val id: Int,
        override val name: String,
        override val groups: List<Group>,
        override val cachedAt: LocalDateTime
    ) : VppId

    data class Active(
        override val id: Int,
        override val name: String,
        override val groups: List<Group>,
        override val cachedAt: LocalDateTime,
        val accessToken: String,
        val schulverwalterAccessToken: String?
    ) : VppId
}