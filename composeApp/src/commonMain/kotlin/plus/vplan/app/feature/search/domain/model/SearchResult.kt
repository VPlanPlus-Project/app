package plus.vplan.app.feature.search.domain.model

sealed class SearchResult(val type: Result) {
    sealed class SchoolEntity(result: Result) : SearchResult(result) {
        data class Group(val group: plus.vplan.app.domain.model.Group) : SchoolEntity(Result.Group)
        data class Teacher(val teacher: plus.vplan.app.domain.model.Teacher) : SchoolEntity(Result.Teacher)
        data class Room(val room: plus.vplan.app.domain.model.Room) : SchoolEntity(Result.Room)
    }
}

enum class Result {
    Group, Teacher, Room
}