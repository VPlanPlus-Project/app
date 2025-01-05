package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.cache.CachedItem
import kotlin.uuid.Uuid

abstract class Profile : CachedItem<Profile> {
    abstract val id: Uuid
    abstract val customName: String?
    abstract val school: Cacheable<School>
    abstract val profileType: ProfileType
    abstract val displayName: String
    abstract val originalName: String
    abstract fun isLessonRelevant(lesson: Lesson): Boolean

    override fun getItemId(): String = this.id.toHexString()

    override fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<Profile>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (this is StudentProfile && !isConfigSatisfiedForStudentProfile(configuration.studentProfile, allowLoading)) return false
        }

        return true

    }

    data class Fetch(
        val studentProfile: CacheableItemSource.FetchConfiguration<StudentProfile> = Ignore(),
        val teacherProfile: CacheableItemSource.FetchConfiguration<TeacherProfile> = Ignore(),
        val roomProfile: CacheableItemSource.FetchConfiguration<RoomProfile> = Ignore(),
    ) : CacheableItemSource.FetchConfiguration.Fetch<Profile>()

    data class StudentProfile(
        override val id: Uuid,
        override val customName: String?,
        val group: Cacheable<Group>,
        val defaultLessons: Map<Cacheable<DefaultLesson>, Boolean>,
        val vppId: VppId.Active?
    ) : Profile() {
        override val school by lazy {
            if (group is Cacheable.Loaded) group.value.school
            else throw IllegalStateException("Opt-in for group")
        }
        override val profileType = ProfileType.STUDENT
        override val displayName by lazy {
            customName ?: run {
                if (group is Cacheable.Loaded) group.value.name
                else throw IllegalStateException("Opt-in for group")
            }
        }
        override val originalName by lazy {
            if (group is Cacheable.Loaded) group.value.name
            else throw IllegalStateException("Opt-in for group")
        }

        override fun isLessonRelevant(lesson: Lesson): Boolean {
            (group as? Cacheable.Loaded) ?: throw IllegalStateException("Opt-in for group@Profile.StudentProfile")
            val isGroupInLesson = this.group.getItemId() in lesson.groups.map { it.getItemId() }
            val hasDefaultLesson = lesson.defaultLesson != null
            val isDefaultLessonEnabled = defaultLessons[lesson.defaultLesson] != false

            val isLessonTimetable = lesson is Lesson.TimetableLesson
            val isLessonAsCourseEnabled = defaultLessons.entries.any { (defaultLesson, enabled) ->
                if (defaultLesson !is Cacheable.Loaded) throw IllegalStateException("Opt-in for DefaultLesson@Profile.StudentProfile")
                if (defaultLesson.value.course != null && defaultLesson.value.course !is Cacheable.Loaded) throw IllegalStateException("Opt-in for DefaultLesson.course@Profile.StudentProfile")
                enabled &&
                        defaultLesson.value.course != null &&
                        defaultLesson.value.course.toValueOrNull()!!.name == lesson.subject
            }
            val isLessonAsDefaultLessonEnabled = defaultLessons.entries.any { (defaultLesson, enabled) ->
                if (defaultLesson !is Cacheable.Loaded) throw IllegalStateException("Opt-in for default lesson@Profile.StudentProfile")
                enabled &&
                        defaultLesson.value.subject == lesson.subject &&
                        listOf(defaultLesson.value.teacher) == lesson.teachers
            }

            return isGroupInLesson && ((hasDefaultLesson && isDefaultLessonEnabled) || (isLessonTimetable && (isLessonAsCourseEnabled || isLessonAsDefaultLessonEnabled)))
        }

        data class Fetch(
            val group: CacheableItemSource.FetchConfiguration<Group> = Ignore(),
            val defaultLessons: CacheableItemSource.FetchConfiguration<DefaultLesson> = Ignore()
        ) : CacheableItemSource.FetchConfiguration.Fetch<StudentProfile>()

        fun isConfigSatisfiedForStudentProfile(
            configuration: CacheableItemSource.FetchConfiguration<StudentProfile>,
            allowLoading: Boolean
        ): Boolean {
            if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return true
            if (configuration is Fetch) {
                if (configuration.group is Group.Fetch && !this.group.isConfigSatisfied(configuration.group, allowLoading)) return false
                if (configuration.defaultLessons is DefaultLesson.Fetch && this.defaultLessons.keys.any { !it.isConfigSatisfied(configuration.defaultLessons, allowLoading) }) return false
            }
            return true
        }

        override fun copyBase(id: Uuid, customName: String?, profileType: ProfileType): Profile {
            if (profileType != ProfileType.STUDENT) throw IllegalArgumentException("Cannot change type of profile")
            return this.copy(
                id = id,
                customName = customName
            )
        }
    }

    data class TeacherProfile(
        override val id: Uuid,
        override val customName: String?,
        val teacher: Teacher
    ) : Profile() {
        override val school = teacher.school
        override val profileType = ProfileType.TEACHER
        override val displayName = customName ?: teacher.name
        override val originalName = teacher.name

        override fun isLessonRelevant(lesson: Lesson): Boolean {
            return this.teacher.getItemId() in lesson.teachers.map { it.getItemId() }
        }

        override fun copyBase(id: Uuid, customName: String?, profileType: ProfileType): Profile {
            if (profileType != ProfileType.TEACHER) throw IllegalStateException("Cannot change type of profile")
            return this.copy(
                id = id,
                customName = customName
            )
        }
    }

    data class RoomProfile(
        override val id: Uuid,
        override val customName: String?,
        val room: Room
    ) : Profile() {
        override val school = room.school
        override val profileType = ProfileType.ROOM
        override val displayName = customName ?: room.name
        override val originalName = room.name

        override fun isLessonRelevant(lesson: Lesson): Boolean {
            return lesson.rooms?.map { it.getItemId() }?.contains(this.getItemId()) == true
        }

        override fun copyBase(id: Uuid, customName: String?, profileType: ProfileType): Profile {
            if (profileType != ProfileType.ROOM) throw IllegalArgumentException("Cannot change type of profile")
            return this.copy(
                id = id,
                customName = customName
            )
        }
    }

    abstract fun copyBase(
        id: Uuid = this.id,
        customName: String? = this.customName,
        profileType: ProfileType = this.profileType,
    ): Profile
}

enum class ProfileType {
    STUDENT, TEACHER, ROOM
}