package plus.vplan.app.feature.search.domain.model

import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.School

sealed class SearchResult(val type: Result) {
    sealed class SchoolEntity(
        result: Result,
        val name: String,
        val school: School
    ) : SearchResult(result) {

        abstract val lessons: List<Lesson>

        data class Group(
            val group: plus.vplan.app.domain.model.Group,
            override val lessons: List<Lesson>
        ) : SchoolEntity(Result.Group, group.name, group.school!!)

        data class Teacher(
            val teacher: plus.vplan.app.domain.model.Teacher,
            override val lessons: List<Lesson>
        ) : SchoolEntity(Result.Teacher, teacher.name, teacher.school!!)

        data class Room(
            val room: plus.vplan.app.domain.model.Room,
            override val lessons: List<Lesson>
        ) : SchoolEntity(Result.Room, room.name, room.school!!)
    }
}

enum class Result {
    Group, Teacher, Room
}