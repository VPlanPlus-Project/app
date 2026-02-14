@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.App
import plus.vplan.app.core.model.DataTag
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Item
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Assessment(
    override val id: Int,
    val creator: AppEntity,
    val createdAt: LocalDateTime,
    val date: LocalDate,
    val isPublic: Boolean,
    val subjectInstanceId: Int,
    val description: String,
    val type: Type,
    val fileIds: List<Int>,
    val cachedAt: Instant
): Item<Int, DataTag>, KoinComponent {
    override val tags: Set<DataTag> = emptySet()

    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()

    enum class Type {
        SHORT_TEST, CLASS_TEST, PROJECT, ORAL, OTHER
    }

    val subjectInstance by lazy { subjectInstanceRepository.findByAlias(Alias(AliasProvider.Vpp, subjectInstanceId.toString(), 1), forceUpdate = false, preferCurrentState = false) }

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