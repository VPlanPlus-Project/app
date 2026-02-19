package plus.vplan.app.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Assessment(
    override val id: Int,
    val creator: AppEntity,
    val createdAt: LocalDateTime,
    val date: LocalDate,
    val isPublic: Boolean,
    val subjectInstanceId: Uuid,
    val description: String,
    val type: Type,
    val fileIds: List<Int>,
    val cachedAt: Instant
): Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()

    enum class Type {
        SHORT_TEST, CLASS_TEST, PROJECT, ORAL, OTHER
    }

    data class AssessmentFile(
        override val id: Int,
        val name: String,
        val assessment: Int,
        val size: Long
    ): Item<Int, DataTag> {
        override val tags: Set<DataTag> = emptySet()
    }
}