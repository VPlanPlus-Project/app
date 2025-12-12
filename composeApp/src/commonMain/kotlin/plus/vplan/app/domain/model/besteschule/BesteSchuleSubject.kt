package plus.vplan.app.domain.model.besteschule

import kotlin.time.Instant

data class BesteSchuleSubject(
    val id: Int,
    val shortName: String,
    val fullName: String,
    val cachedAt: Instant
)