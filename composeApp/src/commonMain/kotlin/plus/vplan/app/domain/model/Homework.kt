package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.DataTag
import plus.vplan.app.core.model.Item
import plus.vplan.app.core.model.Profile
import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed class Homework(
    val creator: AppEntity
) : Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()
    abstract val createdAt: Instant
    abstract val dueTo: LocalDate
    abstract val taskIds: List<Int>
    abstract val subjectInstanceId: Uuid?
    abstract val fileIds: List<Int>
    abstract val cachedAt: Instant
    abstract val groupId: Uuid?

    data class HomeworkTask(
        override val id: Int,
        val content: String,
        val doneByProfiles: List<Uuid>,
        val doneByVppIds: List<Int>,
        val homeworkId: Int,
        val cachedAt: Instant
    ) : Item<Int, DataTag> {
        override val tags: Set<DataTag> = emptySet()

        fun isDone(profile: Profile.StudentProfile) = (profile.id in doneByProfiles && profile.vppId == null) || profile.vppId?.id in doneByVppIds
    }

    data class HomeworkFile(
        override val id: Int,
        val name: String,
        val homework: Int,
        val size: Long,
    ) : Item<Int, DataTag> {
        override val tags: Set<DataTag> = emptySet()
    }

    data class CloudHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: LocalDate,
        override val taskIds: List<Int>,
        override val subjectInstanceId: Uuid?,
        override val fileIds: List<Int>,
        override val cachedAt: Instant,
        override val groupId: Uuid?,
        val isPublic: Boolean,
        val createdById: Int,
    ) : Homework(
        creator = AppEntity.VppId(createdById)
    )

    data class LocalHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: LocalDate,
        override val taskIds: List<Int>,
        override val subjectInstanceId: Uuid?,
        override val groupId: Uuid?,
        override val fileIds: List<Int>,
        override val cachedAt: Instant,
        val createdByProfileId: Uuid
    ) : Homework(
        creator = AppEntity.Profile(createdByProfileId)
    )
}

enum class HomeworkStatus {
    DONE, PENDING, OVERDUE
}