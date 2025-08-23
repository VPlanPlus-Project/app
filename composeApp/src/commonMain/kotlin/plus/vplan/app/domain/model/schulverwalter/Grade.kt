package plus.vplan.app.domain.model.schulverwalter

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.App
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference

data class Grade(
    override val id: Int,
    val value: String?,
    val isOptional: Boolean,
    val isSelectedForFinalGrade: Boolean,
    val subjectId: Int,
    val teacherId: Int,
    val givenAt: LocalDate,
    val collectionId: Int,
    val vppIdId: Int,
    val cachedAt: Instant
): Item<Int, DataTag>, KoinComponent {
    override val tags: Set<DataTag> = emptySet()

    private val vppIdRepository by inject<VppIdRepository>()

    val collection by lazy { App.collectionSource.getById(collectionId) }
    val subject by lazy { App.subjectSource.getById(subjectId) }
    val teacher by lazy { App.schulverwalterTeacherSource.getById(teacherId) }
    val vppId by lazy { vppIdRepository.getById(vppIdId, ResponsePreference.Fast) }
    fun vppId(responsePreference: ResponsePreference) = vppIdRepository.getById(vppIdId, responsePreference)

    val numericValue: Int?
        get() {
            return this.value?.replace("(", "")?.replace(")", "")?.replace("+", "")?.replace("-", "")?.toIntOrNull()
        }
}
