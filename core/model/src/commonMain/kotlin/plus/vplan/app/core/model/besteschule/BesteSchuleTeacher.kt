package plus.vplan.app.core.model.besteschule

import kotlin.time.Instant

data class BesteSchuleTeacher(
    val id: Int,
    val localId: String,
    val forename: String,
    val surname: String,
    val cachedAt: Instant
)