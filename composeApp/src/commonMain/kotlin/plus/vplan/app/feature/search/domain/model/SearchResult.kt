package plus.vplan.app.feature.search.domain.model

import plus.vplan.app.domain.model.Lesson

sealed class SearchResult(val type: Result) {
    sealed class SchoolEntity(
        result: Result
    ) : SearchResult(result) {

        abstract val lessons: List<Lesson>

        data class Group(
            val group: plus.vplan.app.domain.model.Group,
            override val lessons: List<Lesson>
        ) : SchoolEntity(Result.Group)

        data class Teacher(
            val teacher: plus.vplan.app.domain.model.Teacher,
            override val lessons: List<Lesson>
        ) : SchoolEntity(Result.Teacher)

        data class Room(
            val room: plus.vplan.app.domain.model.Room,
            override val lessons: List<Lesson>
        ) : SchoolEntity(Result.Room)
    }
}

enum class Result {
    Group, Teacher, Room
}