package plus.vplan.app.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem
import kotlin.uuid.Uuid

sealed interface Lesson : CachedItem<Lesson> {
    val id: String
    val week: Cacheable<Week>
    val subject: String?
    val teachers: List<Cacheable<Teacher>>
    val rooms: List<Cacheable<Room>>?
    val groups: List<Cacheable<Group>>
    val defaultLesson: Cacheable<DefaultLesson>?
    val lessonTime: Cacheable<LessonTime>

    override fun getItemId(): String = this.id

    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<Lesson>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.week is Week.Fetch && !this.week.isConfigSatisfied(configuration.week, allowLoading)) return false
            if (configuration.teachers is Teacher.Fetch && this.teachers.any { !it.isConfigSatisfied(configuration.teachers, allowLoading)} ) return false
            if (configuration.rooms is Room.Fetch && this.rooms?.any { !it.isConfigSatisfied(configuration.rooms, allowLoading)} == false) return false
            if (configuration.groups is Group.Fetch && !this.groups.any { !it.isConfigSatisfied(configuration.groups, allowLoading) }) return false
            if (configuration.lessonTime is LessonTime.Fetch && !this.lessonTime.isConfigSatisfied(configuration.lessonTime, allowLoading)) return false
        }
        return true
    }

    data class Fetch(
        val week: CacheableItemSource.FetchConfiguration<Week> = Ignore(),
        val teachers: CacheableItemSource.FetchConfiguration<Teacher> = Ignore(),
        val rooms: CacheableItemSource.FetchConfiguration<Room> = Ignore(),
        val groups: CacheableItemSource.FetchConfiguration<Group> = Ignore(),
        val lessonTime: CacheableItemSource.FetchConfiguration<LessonTime> = Ignore()
    ) : CacheableItemSource.FetchConfiguration.Fetch<Lesson>()

    data class TimetableLesson(
        override val id: String,
        val dayOfWeek: DayOfWeek,
        override val week: Cacheable<Week>,
        override val subject: String?,
        override val teachers: List<Cacheable<Teacher>>,
        override val rooms: List<Cacheable<Room>>?,
        override val groups: List<Cacheable<Group>>,
        override val lessonTime: Cacheable<LessonTime>,
        val weekType: String?
    ) : Lesson {
        override val defaultLesson: Cacheable<DefaultLesson>? = null

        constructor(
            dayOfWeek: DayOfWeek,
            week: Cacheable<Week>,
            subject: String?,
            teachers: List<Cacheable<Teacher>>,
            rooms: List<Cacheable<Room>>?,
            groups: List<Cacheable<Group>>,
            lessonTime: Cacheable<LessonTime>,
            weekType: String?
        ) : this(
            id = Uuid.random().toHexString(),
            dayOfWeek = dayOfWeek,
            week = week,
            subject = subject,
            teachers = teachers,
            rooms = rooms,
            groups = groups,
            lessonTime = lessonTime,
            weekType = weekType
        )
    }

    data class SubstitutionPlanLesson(
        override val id: String,
        val date: LocalDate,
        override val week: Cacheable<Week>,
        override val subject: String?,
        val isSubjectChanged: Boolean,
        override val teachers: List<Cacheable<Teacher>>,
        val isTeacherChanged: Boolean,
        override val rooms: List<Cacheable<Room>>,
        val isRoomChanged: Boolean,
        override val groups: List<Cacheable<Group>>,
        override val defaultLesson: Cacheable<DefaultLesson>?,
        override val lessonTime: Cacheable<LessonTime>,
        val info: String?
    ) : Lesson
}