package plus.vplan.app.domain.model

import kotlin.uuid.Uuid

abstract class Profile {
    abstract val id: Uuid
    abstract val customName: String?
    abstract val school: School
    abstract val profileType: ProfileType
    abstract val displayName: String
    abstract val originalName: String
    abstract fun isLessonRelevant(lesson: Lesson): Boolean

    data class StudentProfile(
        override val id: Uuid,
        override val customName: String?,
        val group: Group,
        val defaultLessons: Map<DefaultLesson, Boolean>,
        val vppId: VppId.Active?
    ) : Profile() {
        override val school = group.school
        override val profileType = ProfileType.STUDENT
        override val displayName = customName ?: group.name
        override val originalName = group.name

        override fun isLessonRelevant(lesson: Lesson): Boolean {
            return this.group in lesson.groups && lesson.defaultLesson?.let {
                defaultLessons[it] != false
            } ?: true
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
            return this.teacher in lesson.teachers
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
            return lesson.rooms?.contains(this.room) == true
        }
    }
}

enum class ProfileType {
    STUDENT, TEACHER, ROOM
}