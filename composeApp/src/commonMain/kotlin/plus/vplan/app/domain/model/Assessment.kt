package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item
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

    val subjectInstance by lazy { App.subjectInstanceSource.getById(subjectInstanceId) }

    val files by lazy {
        if (fileIds.isEmpty()) flowOf(emptyList())
        else combine(fileIds.map { App.fileSource.getById(it) }) { it.toList() }
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