package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue

data class Assessment(
    val id: Int,
    val creator: AppEntity,
    val createdAt: LocalDateTime,
    val date: LocalDate,
    val isPublic: Boolean,
    val defaultLessonId: Int,
    val description: String,
    val type: Type,
): Item {
    override fun getEntityId(): String = this.id.toString()

    enum class Type {
        SHORT_TEST, CLASS_TEST, PROJECT, ORAL, OTHER
    }

    var subjectInstanceItem: DefaultLesson? = null
        private set

    suspend fun getSubjectInstanceItem(): DefaultLesson {
        return subjectInstanceItem ?: App.defaultLessonSource.getSingleById(defaultLessonId)!!.also { subjectInstanceItem = it }
    }

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
}