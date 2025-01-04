package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItem
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
        configuration: CacheableItem.FetchConfiguration<Profile>,
        allowLoading: Boolean
    ): Boolean {
        if (configuration is CacheableItem.FetchConfiguration.Ignore) return true
        if (configuration is Fetch) {
            if (configuration.school is School.Fetch && !this.school.isConfigSatisfied(configuration.school, allowLoading)) return false
            if (this is StudentProfile && !isConfigSatisfiedForStudentProfile(configuration.studentProfile, allowLoading)) return false
        }

        return true

    }

    data class Fetch(
        val studentProfile: CacheableItem.FetchConfiguration<StudentProfile> = Ignore(),
        val teacherProfile: CacheableItem.FetchConfiguration<TeacherProfile> = Ignore(),
        val roomProfile: CacheableItem.FetchConfiguration<RoomProfile> = Ignore(),
        val school: CacheableItem.FetchConfiguration<School> = Ignore()
    ) : CacheableItem.FetchConfiguration.Fetch<Profile>()

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
                if (defaultLesson !is Cacheable.Loaded) throw IllegalStateException("Opt-in for default lesson@Profile.StudentProfile")
                enabled &&
                        defaultLesson.value.course != null &&
                        defaultLesson.value.course.name == lesson.subject
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
            val group: CacheableItem.FetchConfiguration<Group> = Ignore(),
            val defaultLessons: CacheableItem.FetchConfiguration<DefaultLesson> = Ignore()
        ) : CacheableItem.FetchConfiguration.Fetch<StudentProfile>()

        fun isConfigSatisfiedForStudentProfile(
            configuration: CacheableItem.FetchConfiguration<StudentProfile>,
            allowLoading: Boolean
        ): Boolean {
            if (configuration is CacheableItem.FetchConfiguration.Ignore) return true
            if (configuration is Fetch) {
                if (configuration.group is Group.Fetch && !this.group.isConfigSatisfied(configuration.group, allowLoading)) return false
                if (configuration.defaultLessons is DefaultLesson.Fetch && this.defaultLessons.keys.any { !it.isConfigSatisfied(configuration.defaultLessons, allowLoading) }) return false
            }
            return true
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
    }
}

enum class ProfileType {
    STUDENT, TEACHER, ROOM
}