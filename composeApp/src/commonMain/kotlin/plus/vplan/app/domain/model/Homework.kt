package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item
import kotlin.uuid.Uuid

sealed class Homework : Item {
    abstract val id: Int
    abstract val createdAt: Instant
    abstract val dueTo: Instant
    abstract val tasks: List<Int>
    abstract val defaultLesson: String?
    abstract val group: Int?
    abstract val files: List<Int>
    override fun getEntityId(): String = this.id.toString()

    var defaultLessonItem: DefaultLesson? = null
        private set

    suspend fun getDefaultLessonItem(): DefaultLesson? {
        return defaultLessonItem ?: defaultLesson?.let { defaultLessonId ->
            App.defaultLessonSource.getSingleById(defaultLessonId).also { defaultLessonItem = it }
        }
    }

    var groupItem: Group? = null
        private set

    suspend fun getGroupItem(): Group? {
        return groupItem ?: group?.let { groupId ->
            App.groupSource.getSingleById(groupId).also { groupItem = it }
        }
    }

    var taskItems: List<HomeworkTask>? = null
        private set

    suspend fun getTaskItems(): List<HomeworkTask> {
        return taskItems ?: tasks.mapNotNull { App.homeworkTaskSource.getSingleById(it) }.also { taskItems = it }
    }

    data class HomeworkTask(
        val id: Int,
        val content: String,
        val doneByProfiles: List<Uuid>,
        val doneByVppIds: List<Int>,
        val homework: Int,
    ) : Item {
        override fun getEntityId(): String = this.id.toString()

        /**
         * Used by viewModels to prepare for the UI by setting this value to the done state of the current profile or vpp.ID
         */
        var isDone: Boolean = false

        fun setIsDone(profile: Profile.StudentProfile) {
            isDone = profile.id in doneByProfiles || profile.vppId in doneByVppIds
        }
    }

    data class HomeworkFile(
        val id: Int,
        val name: String,
        val homework: Int,
        val size: Long,
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
        override val files: List<Int>,
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

        var createdByItem: VppId? = null
            private set

        suspend fun getCreatedBy(): VppId {
            return createdByItem ?: createdBy.let { createdById ->
                App.vppIdSource.getSingleById(createdById)!!.also { createdByItem = it }
            }
        }
    }

    data class LocalHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: Instant,
        override val tasks: List<Int>,
        override val defaultLesson: String?,
        override val files: List<Int>,
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
