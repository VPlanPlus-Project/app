package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItem
import plus.vplan.app.domain.cache.CachedItem

sealed class Homework : CachedItem<Homework> {
    abstract val id: Int
    abstract val createdAt: Instant
    abstract val dueTo: Instant
    abstract val tasks: List<Cacheable<HomeworkTask>>
    abstract val defaultLesson: DefaultLesson?
    abstract val group: Group?

    override fun getItemId(): String = this.id.toString()
    override fun isConfigSatisfied(
        configuration: CacheableItem.FetchConfiguration<Homework>,
        allowLoading: Boolean
    ): Boolean {
        TODO("Not yet implemented")
    }

    data class HomeworkTask(
        val id: Int,
        val content: String,
        val isDone: Boolean?,
        val homework: Cacheable<Homework>
    ) : CachedItem<HomeworkTask> {
        override fun getItemId(): String = this.id.toString()
        override fun isConfigSatisfied(
            configuration: CacheableItem.FetchConfiguration<HomeworkTask>,
            allowLoading: Boolean
        ): Boolean {
            TODO()
        }

        class Fetch(
            homework: CacheableItem.FetchConfiguration<Homework> = Ignore()
        ) : CacheableItem.FetchConfiguration.Fetch<HomeworkTask>()
    }

    data class CloudHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: Instant,
        override val tasks: List<Cacheable<HomeworkTask>>,
        override val defaultLesson: DefaultLesson?,
        override val group: Group?,
        val isPublic: Boolean,
        val createdBy: Cacheable<VppId>,
    ) : Homework()

    data class LocalHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: Instant,
        override val tasks: List<Cacheable<HomeworkTask>>,
        override val defaultLesson: DefaultLesson?,
        val createdByProfile: Profile.StudentProfile
    ) : Homework() {
        override val group: Group = createdByProfile.group
    }

    open class Fetch(
        val tasks: CacheableItem.FetchConfiguration<HomeworkTask> = Ignore(),
        val vppId: CacheableItem.FetchConfiguration<VppId> = Ignore(),
    ) : CacheableItem.FetchConfiguration.Fetch<Homework>()
}
