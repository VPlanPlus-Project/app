package plus.vplan.app.core.model

import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed class Homework: Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()
    abstract val createdAt: Instant
    abstract val dueTo: LocalDate
    abstract val tasks: List<HomeworkTask>
    abstract val subjectInstance: SubjectInstance?
    abstract val files: List<File>
    abstract val cachedAt: Instant
    abstract val group: Group?

    abstract val creator: AppEntity

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
        override val tasks: List<HomeworkTask>,
        override val subjectInstance: SubjectInstance?,
        override val files: List<File>,
        override val cachedAt: Instant,
        override val group: Group?,
        val isPublic: Boolean,
        val createdBy: VppId,
    ) : Homework() {
        override val creator: AppEntity.VppId = AppEntity.VppId(createdBy)
    }

    data class LocalHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: LocalDate,
        override val tasks: List<HomeworkTask>,
        override val subjectInstance: SubjectInstance?,
        override val group: Group?,
        override val files: List<File>,
        override val cachedAt: Instant,
        val createdByProfile: Profile.StudentProfile
    ) : Homework() {
        override val creator: AppEntity.Profile = AppEntity.Profile(createdByProfile)
    }
}

enum class HomeworkStatus {
    DONE, PENDING, OVERDUE
}