package plus.vplan.app.domain.model

import kotlinx.datetime.Instant

sealed class Homework {
    abstract val id: Int
    abstract val createdAt: Instant
    abstract val dueTo: Instant
    abstract val tasks: List<HomeworkTask>
    abstract val defaultLesson: DefaultLesson?
    abstract val group: Group?

    data class HomeworkTask(
        val id: Int,
        val content: String,
        val isDone: Boolean?
    )

    data class CloudHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: Instant,
        override val tasks: List<HomeworkTask>,
        override val defaultLesson: DefaultLesson?,
        override val group: Group?,
        val isPublic: Boolean,
        val createdBy: VppId,
    ) : Homework()

    data class LocalHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: Instant,
        override val tasks: List<HomeworkTask>,
        override val defaultLesson: DefaultLesson?,
        val createdByProfile: Profile.StudentProfile
    ) : Homework() {
        override val group: Group = createdByProfile.group
    }
}
