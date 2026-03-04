package plus.vplan.app.core.model.besteschule

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class BesteSchuleInterval(
    val id: Int,
    val type: Type,
    val name: String,
    val from: LocalDate,
    val to: LocalDate,
    val includedIntervalId: Int?,
    val yearId: Int,
    val linkedToSchulverwalterAccountIds: Set<Int>,
    val collectionIds: Set<Int> = emptySet(),
    val cachedAt: Instant
) {
    sealed class Type {
        abstract val name: String

        data object Sek1: Type() {
            override val name: String = "sek1"
        }
        data object Sek2: Type() {
            override val name: String = "sek2"
        }
        data class Other(val raw: String): Type() {
            override val name: String = raw
        }

        companion object {
            fun fromString(type: String): Type {
                return when (
                    type
                        .lowercase()
                        .replace(" ", "")
                        .replace(".", "")
                ) {
                    "sek1", "seki" -> Sek1
                    "sek2", "sekii", "jg11", "jg12", "jg13" -> Sek2
                    else -> {
                        //captureError("Interval", "Unknown type: $type")
                        Other(type)
                    }
                }
            }
        }
    }
}