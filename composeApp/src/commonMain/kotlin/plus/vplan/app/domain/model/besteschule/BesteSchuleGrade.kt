package plus.vplan.app.domain.model.besteschule

import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.repository.besteschule.BesteSchuleCollectionsRepository
import kotlin.time.Instant

data class BesteSchuleGrade(
    val id: Int,
    val value: String?,
    val isOptional: Boolean,
    val isSelectedForFinalGrade: Boolean,
    val schulverwalterUserId: Int,
    val collectionId: Int,
    val givenAt: LocalDate,
    val cachedAt: Instant
): KoinComponent {
    private val besteSchuleCollectionRepository by inject<BesteSchuleCollectionsRepository>()

    val collection by lazy { besteSchuleCollectionRepository.getFromCache(collectionId) }

    val numericValue: Int?
        get() {
            return this.value?.replace("(", "")?.replace(")", "")?.replace("+", "")?.replace("-", "")?.toIntOrNull()
        }
}