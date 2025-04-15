package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue

data class Assessment(
    val id: Int,
    val creator: AppEntity,
    val createdAt: LocalDateTime,
    val date: LocalDate,
    val isPublic: Boolean,
    val subjectInstanceId: Int,
    val description: String,
    val type: Type,
    val files: List<Int>,
    val cachedAt: Instant
): Item<DataTag> {
    override fun getEntityId(): String = this.id.toString()
    override val tags: Set<DataTag> = emptySet()

    enum class Type {
        SHORT_TEST, CLASS_TEST, PROJECT, ORAL, OTHER
    }

    val subjectInstance by lazy { App.subjectInstanceSource.getById(subjectInstanceId) }

    var createdByVppId: VppId? = null
        private set

    suspend fun getCreatedByVppIdItem(): VppId? {
        if (this.creator !is AppEntity.VppId) return null
        return this.createdByVppId ?: App.vppIdSource.getSingleById(this.creator.id).also { this.createdByVppId = it }
    }

    var createdByProfile: Profile.StudentProfile? = null
        private set

    suspend fun getCreatedByProfileItem(): Profile.StudentProfile? {
        if (this.creator !is AppEntity.Profile) return null
        return this.createdByProfile ?: (App.profileSource.getById(this.creator.id).getFirstValue() as? Profile.StudentProfile)?.also { createdByProfile = it }
    }

    fun getFilesFlow() = combine(files.map { App.fileSource.getById(it).filterIsInstance<CacheState.Done<File>>() }) { it.toList().map { it.data } }

    data class AssessmentFile(
        val id: Int,
        val name: String,
        val assessment: Int,
        val size: Long
    ): Item<DataTag> {
        override fun getEntityId(): String = this.id.toString()
        override val tags: Set<DataTag> = emptySet()
    }
}