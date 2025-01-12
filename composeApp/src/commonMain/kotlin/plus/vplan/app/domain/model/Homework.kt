package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.Item
import kotlin.uuid.Uuid

sealed class Homework : Item {
    abstract val id: Int
    abstract val createdAt: Instant
    abstract val dueTo: Instant
    abstract val tasks: List<Int>
    abstract val defaultLesson: String?
    abstract val group: Int?
    override fun getEntityId(): String = this.id.toString()

    data class HomeworkTask(
        val id: Int,
        val content: String,
        val isDone: Boolean?,
        val homework: Int
    ) : Item {
        override fun getEntityId(): String = this.id.toString()
    }

    data class CloudHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: Instant,
        override val tasks: List<Int>,
        override val defaultLesson: String?,
        override val group: Int?,
        val isPublic: Boolean,
        val createdBy: Int,
    ) : Homework() {
        override fun copyBase(createdAt: Instant, dueTo: Instant, tasks: List<Int>, defaultLesson: String?, group: Int?): Homework {
            return this.copy(
                createdAt = createdAt,
                dueTo = dueTo,
                tasks = tasks,
                defaultLesson = defaultLesson,
                group = group
            )
        }
    }

    data class LocalHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: Instant,
        override val tasks: List<Int>,
        override val defaultLesson: String?,
        val createdByProfile: Uuid
    ) : Homework() {
        override val group: Int
            get() = throw IllegalStateException("Please use the profile to get the group")

        override fun copyBase(createdAt: Instant, dueTo: Instant, tasks: List<Int>, defaultLesson: String?, group: Int?): Homework {
            return this.copy(
                createdAt = createdAt,
                dueTo = dueTo,
                tasks = tasks,
                defaultLesson = defaultLesson
            )
        }
    }

    abstract fun copyBase(
        createdAt: Instant = this.createdAt,
        dueTo: Instant = this.dueTo,
        tasks: List<Int> = this.tasks,
        defaultLesson: String? = this.defaultLesson,
        group: Int? = this.group
    ): Homework
}
