package plus.vplan.app.domain.model

import kotlinx.datetime.Instant
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem

sealed class Homework : CachedItem<Homework> {
    abstract val id: Int
    abstract val createdAt: Instant
    abstract val dueTo: Instant
    abstract val tasks: List<Cacheable<HomeworkTask>>
    abstract val defaultLesson: Cacheable<DefaultLesson>?
    abstract val group: Cacheable<Group>?

    override fun getItemId(): String = this.id.toString()
    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<Homework>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.vppId is VppId.Fetch && this is CloudHomework && !this.createdBy.isConfigSatisfied(configuration.vppId, allowLoading)) return false
            if (configuration.tasks is HomeworkTask.Fetch && this.tasks.any { !it.isConfigSatisfied(configuration.tasks, allowLoading) }) return false
            if (configuration.defaultLesson is DefaultLesson.Fetch && this.defaultLesson?.isConfigSatisfied(configuration.defaultLesson, allowLoading) == false) return false
            if (configuration.group is Group.Fetch && this.group?.isConfigSatisfied(configuration.group, allowLoading) == false) return false
            if (configuration.profile is Profile.StudentProfile.Fetch && this is LocalHomework && !this.createdByProfile.isConfigSatisfied(Profile.Fetch(studentProfile = configuration.profile), allowLoading)) return false
        }
        return true
    }

    data class HomeworkTask(
        val id: Int,
        val content: String,
        val isDone: Boolean?,
        val homework: Cacheable<Homework>
    ) : CachedItem<HomeworkTask> {
        override fun getItemId(): String = this.id.toString()
        override fun isConfigSatisfied(
            configuration: CacheableItemSource.FetchConfiguration<HomeworkTask>,
            allowLoading: Boolean
        ): Boolean {
            if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
            if (configuration is Fetch) {
                if (configuration.homework is Homework.Fetch && !this.homework.isConfigSatisfied(configuration.homework, allowLoading)) return false
            }
            return true
        }

        data class Fetch(
            val homework: CacheableItemSource.FetchConfiguration<Homework> = Ignore()
        ) : CacheableItemSource.FetchConfiguration.Fetch<HomeworkTask>()
    }

    data class CloudHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: Instant,
        override val tasks: List<Cacheable<HomeworkTask>>,
        override val defaultLesson: Cacheable<DefaultLesson>?,
        override val group: Cacheable<Group>?,
        val isPublic: Boolean,
        val createdBy: Cacheable<VppId>,
    ) : Homework()

    data class LocalHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: Instant,
        override val tasks: List<Cacheable<HomeworkTask>>,
        override val defaultLesson: Cacheable<DefaultLesson>?,
        val createdByProfile: Cacheable<Profile>
    ) : Homework() {
        override val group: Cacheable<Group> by lazy {
            if (createdByProfile !is Cacheable.Loaded) throw IllegalStateException("Opt-in for profile@Homework")
            if (createdByProfile.value !is Profile.StudentProfile) throw IllegalStateException("Profile must be student-profile")
            return@lazy createdByProfile.value.group
        }
    }

    data class Fetch(
        val tasks: CacheableItemSource.FetchConfiguration<HomeworkTask> = Ignore(),
        val vppId: CacheableItemSource.FetchConfiguration<VppId> = Ignore(),
        val defaultLesson: CacheableItemSource.FetchConfiguration<DefaultLesson> = Ignore(),
        val group: CacheableItemSource.FetchConfiguration<Group> = Ignore(),
        val profile: CacheableItemSource.FetchConfiguration<Profile.StudentProfile> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<Homework>()
}
